package gomoku.http

import gomoku.http.media.Problem
import gomoku.http.model.lobby.LobbyExitOutputModel
import gomoku.http.utils.HttpTestAssistant.createRandomUser
import gomoku.http.utils.HttpTestAssistant.createToken
import gomoku.http.utils.HttpTestAssistant.findGame
import gomoku.services.game.WaitForGameSuccess
import gomoku.utils.IntrusiveTests
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.TestDataGenerator.newTestId
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RequiresDatabaseConnection
@IntrusiveTests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LobbyTests {

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `can wait in a lobby until a guest joins`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (hostId, registrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, registrationCredentials).token

        // when: joining a lobby (find game)
        val lobbyId = findGame(client, hostToken, true).value

        // and: waiting in the lobby
        // then: the response is a 200 with the proper representation
        val waitingInLobby = client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(WaitForGameSuccess.WaitingInLobby::class.java)
            .returnResult()
            .responseBody!!

        // and: the lobby has valid id and message
        assertEquals(lobbyId, waitingInLobby.id.value)
        assertEquals("Waiting in lobby with id=$lobbyId", waitingInLobby.message)

        // given: a random user to be the guest
        val (_, guestRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val guestToken = createToken(client, guestRegistrationCredentials).token

        // when: joining the lobby
        val gameId = findGame(client, guestToken, false).value

        // and: the host waits in the lobby again
        // then: the response is a 200 with the proper representation
        val waitingInLobbyAfterGuestJoined = client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $guestToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(WaitForGameSuccess.GameMatch::class.java)
            .returnResult()
            .responseBody!!

        // and: the lobby has valid id and message
        assertEquals(gameId, waitingInLobbyAfterGuestJoined.id.value)
        assertEquals("Joined the game successfully with id=$gameId", waitingInLobbyAfterGuestJoined.message)

        // when: waiting in an invalid lobby
        val invalidLobbyId = "-1"
        // then: the response is a 400 with a proper problem
        val waitingInAnInvalidLobby = client.get().uri("/lobby/$invalidLobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, waitingInAnInvalidLobby.type)
        assertEquals("Invalid lobby id", waitingInAnInvalidLobby.title)
        assertEquals("The lobby id must be a positive integer", waitingInAnInvalidLobby.detail)
        assertEquals(URI("/api/lobby/$invalidLobbyId"), waitingInAnInvalidLobby.instance)
        assertEquals(400, waitingInAnInvalidLobby.status)

        // when: waiting in a lobby without joining it first or being in any game
        // then: the response is a 400 with a proper problem
        val waitingInNoLobbyOrInGame = client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userDoesntBelongToAnyGameOrLobby, waitingInNoLobbyOrInGame.type)
        assertEquals("User doesn't belong to any game or lobby", waitingInNoLobbyOrInGame.title)
        assertEquals("The user with id <$hostId> doesn't belong to any game or lobby", waitingInNoLobbyOrInGame.detail)
        val waitingInNoLobbyOrInGameInstance = assertNotNull(waitingInNoLobbyOrInGame.instance)
        assertEquals(URI("/api/lobby/$lobbyId"), waitingInNoLobbyOrInGameInstance)
        assertEquals(403, waitingInNoLobbyOrInGame.status)

        // when: waiting in a lobby that the user is not in
        val anotherLobbyId = findGame(client, hostToken, true).value
        // then: the response is a 400 with a proper problem
        val waitingInAnNonExistingLobby = client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $guestToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userNotInLobby, waitingInAnNonExistingLobby.type)
        assertEquals("User not in lobby", waitingInAnNonExistingLobby.title)
        assertEquals("The user with id <$guestToken> is not in this lobby", waitingInAnNonExistingLobby.detail)
        val waitingInAnNonExistingLobbyInstance = assertNotNull(waitingInAnNonExistingLobby.instance)
        assertEquals(URI("/api/lobby/$anotherLobbyId"), waitingInAnNonExistingLobbyInstance)
        assertEquals(404, waitingInAnNonExistingLobby.status)
        val waitingInAnNonExistingLobbyData = assertNotNull(waitingInAnNonExistingLobby.data)
        assertEquals(
            mapOf(
                "userId" to hostId.value,
                "lobbyId" to anotherLobbyId
            ), waitingInAnNonExistingLobbyData
        )

        // when: waiting in a lobby without authentication
        // then: the response is a 401 with a proper problem
        client.get().uri("/lobby/$lobbyId")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
    }

    @Test
    fun `can exit a lobby`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (hostId, registrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, registrationCredentials).token

        // when: joining a lobby (find game)
        val lobbyId = findGame(client, hostToken, true).value

        // and: exiting the lobby
        // then: the response is a 200 with the proper representation
        val lobbyExit = client.delete().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isCreated
            .expectBody(LobbyExitOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // and: the lobby has valid id and message
        assertEquals(lobbyId, lobbyExit.lobbyId)
        assertEquals("Lobby was exited successfully.", lobbyExit.message)

        // when: exiting an invalid lobby
        val invalidLobbyId = "-1"
        // then: the response is a 400 with a proper problem
        val invalidLobbyIdProblem = client.delete().uri("/lobby/$invalidLobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidLobbyIdProblem.type)
        assertEquals("Invalid lobby id", invalidLobbyIdProblem.title)
        assertEquals("The lobby id must be a positive integer", invalidLobbyIdProblem.detail)
        assertEquals(URI("/api/lobby/$invalidLobbyId"), invalidLobbyIdProblem.instance)
        assertEquals(400, invalidLobbyIdProblem.status)

        // when: exiting a lobby that doesn't exist
        val randomLobbyId = newTestId().value
        // then: the response is a 400 with a proper problem
        val nonExistingLobbyProblem = client.delete().uri("/lobby/$invalidLobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isOk
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.lobbyNotFound, nonExistingLobbyProblem.type)
        assertEquals("Requested lobby was not found", nonExistingLobbyProblem.title)
        assertEquals("The lobby with id <$randomLobbyId> was not found", nonExistingLobbyProblem.detail)
        assertEquals(URI("/api/lobby/$randomLobbyId"), nonExistingLobbyProblem.instance)
        assertEquals(404, nonExistingLobbyProblem.status)

        // when: exiting a lobby without authentication
        // then: the response is a 401 with a proper problem
        client.delete().uri("/lobby/$lobbyId")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
    }
}