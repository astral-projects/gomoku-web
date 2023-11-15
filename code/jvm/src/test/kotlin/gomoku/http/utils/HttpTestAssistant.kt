package gomoku.http.utils

import gomoku.domain.components.Id
import gomoku.http.model.IdOutputModel
import gomoku.http.model.RegistrationCredentials
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.services.GameServicesTests
import gomoku.services.game.FindGameSuccess
import gomoku.utils.TestDataGenerator
import gomoku.utils.get
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
    private fun getTestVariantId(): Id = Id(1).get()// GameServicesTests.gameTestVariant.id

    /*
     * Creates a lobby or a game and returns the id of the created lobby or game, or fails the test
     * if not successful.
     * @param client the HTTP client to send the request.
     * @param token the token of the user.
     * @param isHost whether the user is the host of the game or not.
     * @return the id of the created lobby or game.
     */
    fun findGame(
        client: WebTestClient,
        token: String,
        isHost: Boolean,
    ): Id {
        // when: when joining a lobby (find game)
        // then: the response is a 200 with the proper representation
        val findGameClass =
            if (isHost) FindGameSuccess.LobbyCreated::class.java else FindGameSuccess.GameMatch::class.java
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

        assertIs<FindGameSuccess.LobbyCreated>(findGameSuccess)

        // and: the id and message are those expected
        val id = findGameSuccess.id
        println("id = $id")
        assertTrue(id.value > 0)
        assertEquals(
            if (isHost) "Lobby created successfully with id=${id.value}" else "Joined the game successfully with id=${id.value}",
            findGameSuccess.message
        )
        return id
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
        usernameSuffix: String? = null,
    ): Pair<Id, RegistrationCredentials> {
        // and: a random user
        val actualUsername = TestDataGenerator.newTestUserName().value + (usernameSuffix ?: "")
        val password = TestDataGenerator.newTestPassword().value
        val email = TestDataGenerator.newTestEmail().value

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        val userId = client.post().uri("/users")
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
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertTrue(userId.id > 0)

        return Id(userId.id).get() to RegistrationCredentials(
            username = actualUsername,
            password = password,
            email = email
        )

    }

    /**
     * Creates a token for the given user registration credentials or fails the test if not successful.
     * @param client the HTTP client to send the request.
     * @param registrationCredentials the registration credentials of the user.
     * @returns the token output model.
     */
    fun createToken(
        client: WebTestClient,
        registrationCredentials: RegistrationCredentials,
    ): UserTokenCreateOutputModel {
        // when: creating a token
        // then: the response is a 200
        val tokenOutputModel = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to registrationCredentials.username,
                    "password" to registrationCredentials.password
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(UserTokenCreateOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // and: the token is valid
        assertNotNull(tokenOutputModel.token)
        return tokenOutputModel
    }
}