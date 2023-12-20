package gomoku.http

import com.fasterxml.jackson.databind.ObjectMapper
import gomoku.domain.game.GameState
import gomoku.http.media.Problem
import gomoku.http.utils.HttpTestAssistant.createRandomUser
import gomoku.http.utils.HttpTestAssistant.createToken
import gomoku.http.utils.HttpTestAssistant.findGame
import gomoku.http.utils.HttpTestAssistant.getTestVariantId
import gomoku.utils.IntrusiveTests
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.TestDataGenerator.newTestId
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RequiresDatabaseConnection
@IntrusiveTests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameTests {
    @LocalServerPort
    var port: Int = 0

    val variantId = getTestVariantId().value

    @Test
    fun `can join lobby and create game`() {
        // given: an HTTP client and pushing the variant to the database
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (_, hostRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, hostRegistrationCredentials)

        // when: a user tries to find a game not auhtenticated
        // then: the response is a 401 with the proper problem
        client.post().uri("/games")
            .header("Authorization", "Bearer invalid-token")
            .bodyValue(
                mapOf(
                    "variantId" to variantId
                )
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: a user tries to find a game
        // then: the response is a 201 with the proper body
        val lobbyCreated = client.post().uri("/games")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(
                mapOf(
                    "variantId" to variantId
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("Content-Type") {
                assertEquals("application/vnd.siren+json", it)
            }
            .expectBody()
            .jsonPath("$.properties.id").isNumber
            .jsonPath("$.properties.message").isNotEmpty
            .returnResult()
            .responseBody!!

        // and: creating a json node from the response body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(lobbyCreated)

        val lobbyId = jsonNode.path("properties").path("id").asInt()
        val message = jsonNode.path("properties").path("message").asText()

        assertTrue(lobbyId > 0)
        assertTrue(message.startsWith("Lobby created successfully"))

        // and: a random user to be the guest
        val (_, guestRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val guestToken = createToken(client, guestRegistrationCredentials)

        // when: a user tries to find a game
        // then: the response is a 201 with the proper body
        val gameMatch = client.post().uri("/games")
            .header("Authorization", "Bearer $guestToken")
            .bodyValue(
                mapOf(
                    "variantId" to variantId
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("Content-Type") {
                assertEquals("application/vnd.siren+json", it)
            }
            .expectBody()
            .jsonPath("$.properties.id").isNumber
            .jsonPath("$.properties.message").isNotEmpty
            .returnResult()
            .responseBody!!

        val jsonNode2 = objectMapper.readTree(gameMatch)

        val gameId = jsonNode2.path("properties").path("id").asInt()
        val messageGame = jsonNode2.path("properties").path("message").asText()

        assertTrue(gameId > 0)
        assertTrue(messageGame.startsWith("Joined the game successfully"))

        // when: a user tries to find a game with an invalid variant id
        val invalidVariantId = "-1"
        // then: the response is a 404 with the proper problem
        val invalidVariantIdProblem = client.post().uri("/games")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(
                mapOf(
                    "variantId" to invalidVariantId
                )
            )
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().value("Content-Type") {
                assertEquals("application/problem+json", it)
            }
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidVariantIdProblem.type)
        assertEquals("Invalid variant id", invalidVariantIdProblem.title)
        assertEquals("The variant id must be a positive integer", invalidVariantIdProblem.detail)
        assertEquals(URI("/api/games"), invalidVariantIdProblem.instance)
        assertEquals(404, invalidVariantIdProblem.status)

        // when: a user tries to find a game with a variant that doesn't exist
        val nonExistentVariantId = newTestId().value
        // then: the response is a 404 with the proper problem
        val nonExistingVariantProblem = client.post().uri("/games")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(
                mapOf(
                    "variantId" to nonExistentVariantId
                )
            )
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.gameVariantNotFound, nonExistingVariantProblem.type)
        assertEquals("Game variant not found", nonExistingVariantProblem.title)
        assertEquals("The game variant with id <$nonExistentVariantId> was not found", nonExistingVariantProblem.detail)
        assertEquals(URI("/api/games"), nonExistingVariantProblem.instance)
        assertEquals(404, nonExistingVariantProblem.status)
    }

    @Test
    fun `can get a game by id`() {
        // given: an HTTP client and pushing the variant to the database
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (hostId, hostRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, hostRegistrationCredentials)

        // and: a user tries to find a game
        findGame(client, hostToken, true)

        // and: guest joins the lobby
        val (guestId, guestRegistrationCredentials) = createRandomUser(client)
        val guestToken = createToken(client, guestRegistrationCredentials)
        val gameId = findGame(client, guestToken, false)

        // when: a user tries to get the game by id
        // then: the response is a 200 with the proper body
        client.get().uri("/games/$gameId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties.id").isEqualTo(gameId)
            .jsonPath("$.properties.state.name").isEqualTo(GameState.IN_PROGRESS.name.lowercase(Locale.getDefault()))
            .jsonPath("$.properties.variant.id").isEqualTo(variantId)
            .jsonPath("$.properties.hostId").isEqualTo(hostId)
            .jsonPath("$.properties.guestId").isEqualTo(guestId)
            .returnResult()
            .responseBody!!

        // when: a user tries to get a game by an invalid id
        val invalidGameId = "-1"
        // then: the response is a 404 with the proper problem
        val invalidGameIdProblem = client.get().uri("/games/$invalidGameId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidGameIdProblem.type)
        assertEquals("Invalid game id", invalidGameIdProblem.title)
        assertEquals("The game id must be a positive integer", invalidGameIdProblem.detail)
        assertEquals(URI("/api/games/$invalidGameId"), invalidGameIdProblem.instance)
        assertEquals(404, invalidGameIdProblem.status)

        // when: a user tries to get a game by an id that doesn't exist
        val nonExistentGameId = newTestId().value
        // then: the response is a 404 with the proper problem
        val nonExistentGameProblem = client.get().uri("/games/$nonExistentGameId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.gameNotFound, nonExistentGameProblem.type)
        assertEquals("Game was not found", nonExistentGameProblem.title)
        assertEquals("The game with id <$nonExistentGameId> was not found", nonExistentGameProblem.detail)
        assertEquals(URI("/api/games/$nonExistentGameId"), nonExistentGameProblem.instance)
        assertEquals(404, nonExistentGameProblem.status)
    }

    @Test
    fun `an user can exit a game`() {
        // given: an HTTP client and pushing the variant to the database
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (hostId, hostRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, hostRegistrationCredentials)

        // and: a user tries to find a game
        findGame(client, hostToken, true)

        // and: guest joins the lobby
        val (guestId, guestRegistrationCredentials) = createRandomUser(client)
        val guestToken = createToken(client, guestRegistrationCredentials)
        val gameId = findGame(client, guestToken, false)

        // when: a third user tries to exit the game
        val (thirdUserId, thirdRegistrationCredentials) = createRandomUser(client)
        val thirdToken = createToken(client, thirdRegistrationCredentials)

        // then: the response is a 400 with the proper body
        val userDoesntBelongToThisGameProblem = client.post().uri("/games/$gameId/exit")
            .header("Authorization", "Bearer $thirdToken")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userDoesntBelongToThisGame, userDoesntBelongToThisGameProblem.type)
        assertEquals("User doesn't belong to this game", userDoesntBelongToThisGameProblem.title)
        assertEquals(
            "The user with id <$thirdUserId> doesn't belong to the game.",
            userDoesntBelongToThisGameProblem.detail
        )
        assertEquals(URI("/api/games/$gameId/exit"), userDoesntBelongToThisGameProblem.instance)
        assertEquals(400, userDoesntBelongToThisGameProblem.status)

        // when: a guest or host tries to exit the game
        // then: the response is a 200 with the proper body
        val playerToken = if (Random().nextBoolean()) guestToken else hostToken
        val idOfPlayerToken = if (playerToken == guestToken) guestId else hostId
        client.post().uri("/games/$gameId/exit")
            .header("Authorization", "Bearer $playerToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties.gameId").isEqualTo(gameId)
            .jsonPath("$.properties.message")
            .isEqualTo("User with id <$idOfPlayerToken> left the Game with id <$gameId>.")
            .returnResult()
            .responseBody!!

        // when: a user tries to exit the game again
        // then: the response is a 400 with the proper body
        val exitAlreadyFinishedGame = client.post().uri("/games/$gameId/exit")
            .header("Authorization", "Bearer $guestToken")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.gameAlreadyFinished, exitAlreadyFinishedGame.type)
        assertEquals("Game already finished", exitAlreadyFinishedGame.title)
        assertEquals("The game with id <$gameId> is already finished", exitAlreadyFinishedGame.detail)
        assertEquals(URI("/api/games/$gameId/exit"), exitAlreadyFinishedGame.instance)
        assertEquals(400, exitAlreadyFinishedGame.status)

        // when: a user tries to exit the game by an invalid id
        val invalidGameId = "-1"
        // then: the response is a 404 with the proper problem
        val invalidGameIdProblem = client.post().uri("/games/$invalidGameId/exit")
            .header("Authorization", "Bearer $guestToken")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidGameIdProblem.type)
        assertEquals("Invalid game id", invalidGameIdProblem.title)
        assertEquals("The game id must be a positive integer", invalidGameIdProblem.detail)
        assertEquals(URI("/api/games/$invalidGameId/exit"), invalidGameIdProblem.instance)
        assertEquals(404, invalidGameIdProblem.status)

        // when: a user tries to exit a game by an id that doesn't exist
        val nonExistentGameId = newTestId().value
        // then: the response is a 404 with the proper problem
        val nonExistentGameProblem = client.post().uri("/games/$nonExistentGameId/exit")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.gameNotFound, nonExistentGameProblem.type)
        assertEquals("Game was not found", nonExistentGameProblem.title)
        assertEquals("The game with id <$nonExistentGameId> was not found", nonExistentGameProblem.detail)
        assertEquals(URI("/api/games/$nonExistentGameId/exit"), nonExistentGameProblem.instance)
        assertEquals(404, nonExistentGameProblem.status)
    }

    @Test
    fun `can retrieve all available variants`() {
        // given: an HTTP client and pushing the variant to the database
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // when: a user tries to get the list of variants
        // then: the response is a 200 with the proper body
        val getVariantsResponse = client.get().uri("/games/variants")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties").isArray
            .returnResult()
            .responseBody!!

        // when: creating a json node from the response body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(getVariantsResponse)

        val variants = jsonNode.path("properties")

        assertTrue(getVariantsResponse.isNotEmpty())
        assertTrue(variants.toList().find { it.path("id").path("value").asInt() == variantId } != null)
    }

    @Test
    fun `can make a move on the board`() {
        // given: an HTTP client and pushing the variant to the database
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (hostId, hostRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, hostRegistrationCredentials)

        // and: a user tries to find a game
        findGame(client, hostToken, true)

        // and: guest joins the lobby
        val (guestId, guestRegistrationCredentials) = createRandomUser(client)
        val guestToken = createToken(client, guestRegistrationCredentials)
        val gameId = findGame(client, guestToken, false)

        // when: a user tries to make a move on the board
        // then: the response is a 200 with the proper body
        val validMoveCol = "a"
        val validMoveRow = 7
        val firstMove = mapOf(
            "col" to validMoveCol,
            "row" to validMoveRow
        )
        client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(firstMove)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties.id").isEqualTo(gameId)
            .jsonPath("$.properties.state.name").isEqualTo(GameState.IN_PROGRESS.name.lowercase(Locale.getDefault()))
            .jsonPath("$.properties.variant.id").isEqualTo(variantId)
            .jsonPath("$.properties.hostId").isEqualTo(hostId)
            .jsonPath("$.properties.guestId").isEqualTo(guestId)
            .jsonPath("$.properties.board.grid").isArray
            .jsonPath("$.properties.board.grid[0]").isEqualTo("a7-w")
            .jsonPath("$.properties.board.turn.player").isEqualTo("B")
            .returnResult()
            .responseBody!!

        // when: a user tries to make a move on the board again
        // then: the response is a 400 with the proper body
        val sameMoveProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(firstMove)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.notYourTurn, sameMoveProblem.type)
        assertEquals("Not your turn", sameMoveProblem.title)
        assertEquals(URI("/api/games/$gameId/move"), sameMoveProblem.instance)
        assertEquals(400, sameMoveProblem.status)

        // when: the other user tries to make a move an invalid move on the board
        val invalidMoveCol = "a"
        val invalidMoveRow = 1001021
        val invalidMove = mapOf(
            "col" to invalidMoveCol,
            "row" to invalidMoveRow
        )

        // then: the response is a 400 with the proper body
        val invalidMoveProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $guestToken")
            .bodyValue(invalidMove)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidMove, invalidMoveProblem.type)
        assertEquals("Invalid position", invalidMoveProblem.title)
        assertEquals("The position <$invalidMoveCol, $invalidMoveRow> is invalid", invalidMoveProblem.detail)
        assertEquals(URI("/api/games/$gameId/move"), invalidMoveProblem.instance)
        assertEquals(400, invalidMoveProblem.status)
        assertEquals(mapOf("gameId" to gameId), invalidMoveProblem.data)

        // when: a user tries to make a move on the board in the same position
        // then: the response is a 400 with the proper body
        val samePositionProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $guestToken")
            .bodyValue(firstMove)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.positionTaken, samePositionProblem.type)
        assertEquals("Position taken", samePositionProblem.title)
        assertEquals("The position <$validMoveCol, $validMoveRow> is already taken", samePositionProblem.detail)
        assertEquals(URI("/api/games/$gameId/move"), samePositionProblem.instance)
        assertEquals(400, samePositionProblem.status)
        assertEquals(mapOf("gameId" to gameId), samePositionProblem.data)

        // when: a another user tries to make plays in this game
        val (outsiderId, outsiderRegistrationCredentials) = createRandomUser(client)
        val outsiderToken = createToken(client, outsiderRegistrationCredentials)

        // then: the response is a 400 with the proper body
        val outsiderMoveProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $outsiderToken")
            .bodyValue(firstMove)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userNotInGame, outsiderMoveProblem.type)
        assertEquals("User not in game", outsiderMoveProblem.title)
        assertEquals("The user with id <$outsiderId> is not in the game.", outsiderMoveProblem.detail)
        assertEquals(URI("/api/games/$gameId/move"), outsiderMoveProblem.instance)
        assertEquals(404, outsiderMoveProblem.status)
        assertEquals(
            mapOf(
                "userId" to outsiderId,
                "gameId" to gameId
            ),
            outsiderMoveProblem.data
        )

        // when: a user tries to make a move on game that doesn't exist
        val nonExistentGameId = newTestId().value

        // then: the response is a 404 with the proper problem
        val gameNotFoundProblem = client.post().uri("/games/$nonExistentGameId/move")
            .header("Authorization", "Bearer $guestToken")
            .bodyValue(firstMove)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.gameNotFound, gameNotFoundProblem.type)
        assertEquals("Game was not found", gameNotFoundProblem.title)
        assertEquals("The game with id <$nonExistentGameId> was not found", gameNotFoundProblem.detail)
        assertEquals(URI("/api/games/$nonExistentGameId/move"), gameNotFoundProblem.instance)
        assertEquals(404, gameNotFoundProblem.status)

        // when: one of the users exists the game
        // then: the response is a 200 with the proper body
        val playerToken = if (Random().nextBoolean()) guestToken else hostToken
        val idOfPlayerToken = if (playerToken == guestToken) guestId else hostId
        client.post().uri("/games/$gameId/exit")
            .header("Authorization", "Bearer $playerToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties.gameId").isEqualTo(gameId)
            .jsonPath("$.properties.message")
            .isEqualTo("User with id <$idOfPlayerToken> left the Game with id <$gameId>.")
            .returnResult()
            .responseBody!!

        // when: one of the users tries to make a move on a game that is already finished
        // then: the response is a 400 with the proper problem
        val gameAlreadyFinishedProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $playerToken")
            .bodyValue(firstMove)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.gameAlreadyFinished, gameAlreadyFinishedProblem.type)
        assertEquals("Game already finished", gameAlreadyFinishedProblem.title)
        assertEquals("The game with id <$gameId> is already finished", gameAlreadyFinishedProblem.detail)
        assertEquals(URI("/api/games/$gameId/move"), gameAlreadyFinishedProblem.instance)
        assertEquals(400, gameAlreadyFinishedProblem.status)

        // when: a user tries to make a move on the board with an invalid row
        val invalidRow = 0
        // then: the response is a 400 with the proper problem
        val invalidRowProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(
                mapOf(
                    "col" to "a",
                    "row" to invalidRow
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        // because of Spring validation
        assertEquals(Problem.invalidRequestContent, invalidRowProblem.type)
        assertEquals("Method argument not valid", invalidRowProblem.title)
        assertEquals(400, invalidRowProblem.status)

        // when: a user tries to make a move on the board with an invalid column
        val invalidColumn = "-"

        // then: the response is a 400 with the proper problem
        val invalidColumnProblem = client.post().uri("/games/$gameId/move")
            .header("Authorization", "Bearer $hostToken")
            .bodyValue(
                mapOf(
                    "col" to invalidColumn,
                    "row" to 10
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidColumn, invalidColumnProblem.type)
        assertEquals("Invalid column", invalidColumnProblem.title)
        assertEquals("The column must be a letter between a and z", invalidColumnProblem.detail)
        assertEquals(URI("/api/games/$gameId/move"), invalidColumnProblem.instance)
        assertEquals(400, invalidColumnProblem.status)
    }
}
