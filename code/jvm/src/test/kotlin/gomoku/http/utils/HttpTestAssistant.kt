package gomoku.http.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import gomoku.domain.components.Id
import gomoku.http.model.TestRegistrationCredentials
import gomoku.http.model.game.GameExitOutputModel
import gomoku.services.GameServicesTests
import gomoku.services.game.FindGameSuccess
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestPassword
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.get
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Provides utility functions for testing the HTTP layer.
 * These functions aim to eliminate code duplication and simplify the process of creating a state
 * that other tests can utilize, while maintaining clarity in the test code regarding the focus of
 * the testing. They should not be used to directly test controller functionality on success,
 * which is the behavior they aim to emulate.
 */
object HttpTestAssistant {

    /**
     * Inserts the test variant into the database and returns its id, as specified in the
     * [GameServicesTests.gameTestVariant] property.
     */
    fun getTestVariantId(): Id = Id(1).get() // GameServicesTests.gameTestVariant.id

    /**
     * Creates a lobby or a game and returns the id of the created lobby or game, or fails the test
     * if not successful.
     * @param client the HTTP client to send the request.
     * @param token the token of the user.
     * @param expectLobbyCreation whether the request is expected to create a lobby or not.
     * @return the id of the created lobby or game.
     */
    fun findGame(
        client: WebTestClient,
        token: String,
        expectLobbyCreation: Boolean
    ): Int {
        // when: when joining a lobby (find game)
        // then: the response is a 201 with the proper representation
        val findGameClass =
            if (expectLobbyCreation) FindGameSuccess.LobbyCreated::class.java else FindGameSuccess.GameMatch::class.java
        val findGameSuccess = client.post().uri("/games")
            .bodyValue(
                mapOf(
                    "variantId" to getTestVariantId().value
                )
            )
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isCreated
            .expectBody(findGameClass)
            .returnResult()
            .responseBody!!

        if (expectLobbyCreation) {
            assertTrue(findGameSuccess is FindGameSuccess.LobbyCreated)
        } else {
            assertTrue(findGameSuccess is FindGameSuccess.GameMatch)
        }

        val id = findGameSuccess.id
        assertTrue(id > 0)
        assertEquals(
            if (expectLobbyCreation) "Lobby created successfully with id=$id" else "Joined the game successfully with id=$id",
            findGameSuccess.message
        )
        return id
    }

    /**
     * Exits the game with the given id or fails the test if not successful.
     * Should only be called after calling [findGame] twice.
     * @param client the HTTP client to send the request.
     * @param token the token of the user.
     * @param gameId the id of the game to exit.
     */
    fun exitGame(
        client: WebTestClient,
        token: String,
        gameId: Int
    ) {
        // when: a user exits the game
        // then: the response is a 200 with the proper representation
        val exitGameModel = client.post().uri("/games/$gameId/exit")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(GameExitOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(exitGameModel.gameId, gameId)
    }

    /**
     * Creates a random user or fails the test if not successful.
     * @param client the HTTP client to send the request.
     * @param usernameSuffix an optional suffix to append to the username. This is useful to
     * generate random usernames that have some point of similarity. Defaults to **null**.
     * @return the id of the created user.
     */
    fun createRandomUser(
        client: WebTestClient,
        usernameSuffix: String? = null
    ): Pair<Int, TestRegistrationCredentials> {
        // and: a random user
        val actualUsername = newTestUserName().value + (usernameSuffix ?: "")
        val password = newTestPassword().value
        val email = newTestEmail().value

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        val responseBody = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to actualUsername,
                    "password" to password,
                    "email" to email
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }
            .expectBody()
            .jsonPath("$.properties.id").isNotEmpty
            .returnResult()
            .responseBody!!

        // and: creating a json node from the response body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)

        val userId = jsonNode.path("properties").path("id").asInt()
        assertTrue(userId > 0)

        return userId to TestRegistrationCredentials(actualUsername, password, email)
    }

    /**
     * Creates a token for the given user registration credentials or fails the test if not successful.
     * @param client the HTTP client to send the request.
     * @param registrationCredentials the registration credentials of the user.
     * @returns the token output model.
     */
    fun createToken(
        client: WebTestClient,
        registrationCredentials: TestRegistrationCredentials
    ): String {
        // when: creating a token
        // then: the response is a 200
        val responseBody = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to registrationCredentials.username,
                    "password" to registrationCredentials.password
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.properties.token").isNotEmpty
            .returnResult()
            .responseBody!!

        // and: creating a json node from the response body
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(responseBody)

        val token = jsonNode.path("properties").path("token").asText()

        // and: the token is valid
        assertNotNull(token)
        return token.toString()
    }

    /**
     * Finds the href of the given position in the links array of the given json node.
     * @param jsonNode the json node to search in.
     * @param position the position to search for
     *  it can be one of the following: "self", "next", "previous", "first", "last".
     * @return the href of the given position or **null** if not found.
     */
    fun findHref(jsonNode: JsonNode, position: String): String? {
        return jsonNode
            .path("links")
            .elements()
            .asSequence() // for performance reasons
            .filter { link ->
                val rel = link.path("rel")
                rel.isArray && rel.size() > 0 && rel[0].asText() == position
            }
            .firstOrNull()
            ?.path("href")?.asText()
            ?.substringAfter("/api") // remove the /api prefix
    }
}
