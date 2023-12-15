package gomoku.http

import com.fasterxml.jackson.databind.ObjectMapper
import gomoku.http.media.Problem
import gomoku.http.utils.HttpTestAssistant.createRandomUser
import gomoku.http.utils.HttpTestAssistant.createToken
import gomoku.http.utils.HttpTestAssistant.findGame
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
        val (_, registrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, registrationCredentials)

        // when: joining a lobby (find game)
        val lobbyId = findGame(client, hostToken, true)

        // and: waiting in the lobby
        // then: the response is a 200 with the proper representation
        client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties.id").isEqualTo(lobbyId)
            .jsonPath("$.properties.message").isEqualTo("Waiting in lobby with id <$lobbyId>")
            .jsonPath("$.actions").isNotEmpty
            .returnResult()
            .responseBody!!

        // given: a random user to be the guest
        val (guestId, guestRegistrationCredentials) = createRandomUser(client)

        // and: a token
        val guestToken = createToken(client, guestRegistrationCredentials)

        // when: waiting in a lobby without joining it first or being in any game
        // then: the response is a 403 with a proper problem
        val waitingInNoLobbyOrInGame = client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $guestToken")
            .exchange()
            .expectStatus().isForbidden
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userDoesntBelongToAnyGameOrLobby, waitingInNoLobbyOrInGame.type)
        assertEquals("User doesn't belong to lobby <$lobbyId>", waitingInNoLobbyOrInGame.title)
        assertEquals("The user with id <$guestId> doesn't belong to lobby <$lobbyId>", waitingInNoLobbyOrInGame.detail)
        val waitingInNoLobbyOrInGameInstance = assertNotNull(waitingInNoLobbyOrInGame.instance)
        assertEquals(URI("/api/lobby/$lobbyId"), waitingInNoLobbyOrInGameInstance)
        assertEquals(403, waitingInNoLobbyOrInGame.status)

        // when: joining the lobby
        val gameId = findGame(client, guestToken, false)

        // and: the host waits in the lobby again
        // then: the response is a 200 with the proper representation
        val waitingInLobbyAfterGuestJoined = client.get().uri("/lobby/$lobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isOk
            .expectHeader().value("Content-Type") {
                assertEquals("application/vnd.siren+json", it)
            }
            .expectBody()
            .jsonPath("$.properties.id").isEqualTo(gameId)
            .jsonPath("$.properties.message").isEqualTo("Already in game with id <$gameId>")
            .returnResult()
            .responseBody!!

        // and: creating a json node from the response body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(waitingInLobbyAfterGuestJoined)

        jsonNode.path("properties").path("message").asText()

        // when: waiting in an invalid lobby
        val invalidLobbyId = "-1"
        // then: the response is a 404 with a proper problem
        val waitingInAnInvalidLobby = client.get().uri("/lobby/$invalidLobbyId")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, waitingInAnInvalidLobby.type)
        assertEquals("Invalid lobby id", waitingInAnInvalidLobby.title)
        assertEquals("The lobby id must be a positive integer", waitingInAnInvalidLobby.detail)
        assertEquals(URI("/api/lobby/$invalidLobbyId"), waitingInAnInvalidLobby.instance)
        assertEquals(404, waitingInAnInvalidLobby.status)

        // when: waiting in a lobby without authentication
        // then: the response is a 401 with a proper problem
        client.get().uri("/lobby/$lobbyId")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
            .expectBody()
    }

    @Test
    fun `can exit a lobby`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user to be the host
        val (_, registrationCredentials) = createRandomUser(client)

        // and: a token
        val hostToken = createToken(client, registrationCredentials)

        // when: joining a lobby (find game)
        val lobbyId = findGame(client, hostToken, true)

        // and: exiting the lobby
        // then: the response is a 200 with the proper representation
        client.delete().uri("/lobby/$lobbyId/exit")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isOk
            .expectHeader().value("Content-Type") {
                assertEquals("application/vnd.siren+json", it)
            }
            .expectBody()
            .jsonPath("$.properties.lobbyId").isEqualTo(lobbyId)
            .jsonPath("$.properties.message").isEqualTo("Lobby was exited successfully.")
            .returnResult()
            .responseBody!!

        // when: exiting an invalid lobby
        val invalidLobbyId = "-1"
        // then: the response is a 404 with a proper problem
        val invalidLobbyIdProblem = client.delete().uri("/lobby/$invalidLobbyId/exit")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().value("Content-Type") {
                assertEquals("application/problem+json", it)
            }
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidLobbyIdProblem.type)
        assertEquals("Invalid lobby id", invalidLobbyIdProblem.title)
        assertEquals("The lobby id must be a positive integer", invalidLobbyIdProblem.detail)
        assertEquals(URI("/api/lobby/$invalidLobbyId/exit"), invalidLobbyIdProblem.instance)
        assertEquals(404, invalidLobbyIdProblem.status)

        // when: exiting a lobby that doesn't exist
        val randomLobbyId = newTestId().value
        // then: the response is a 404 with a proper problem
        val nonExistingLobbyProblem = client.delete().uri("/lobby/$randomLobbyId/exit")
            .header("Authorization", "Bearer $hostToken")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.lobbyNotFound, nonExistingLobbyProblem.type)
        assertEquals("Requested lobby was not found", nonExistingLobbyProblem.title)
        assertEquals("The lobby with id <$randomLobbyId> was not found", nonExistingLobbyProblem.detail)
        assertEquals(URI("/api/lobby/$randomLobbyId/exit"), nonExistingLobbyProblem.instance)
        assertEquals(404, nonExistingLobbyProblem.status)

        // when: exiting a lobby without authentication
        // then: the response is a 401 with a proper problem
        client.delete().uri("/lobby/$lobbyId/exit")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
            .expectBody()
    }
}
