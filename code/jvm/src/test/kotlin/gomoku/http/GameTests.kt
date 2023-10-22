package gomoku.http

import gomoku.domain.user.Email
import gomoku.domain.user.Password
import gomoku.domain.user.Username
import gomoku.http.model.IdOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.services.game.FindGameSuccess
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestPassword
import gomoku.utils.TestDataGenerator.newTestUserName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameTests {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun `test two users finding a game and starting that game`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val username1 = newTestUserName()
        val password1 = newTestPassword()
        val email1 = newTestEmail()

        val username2 = newTestUserName()
        val password2 = newTestPassword()
        val email2 = newTestEmail()

        // when: creating an user and logging in
        val user1 = creatingUser(username1, password1, email1, client)
        val user2 = creatingUser(username2, password2, email2, client)
        val token1 = loggingUser(username1, password1, client)
        val token2 = loggingUser(username2, password2, client)

        // and: someone that is not logged in tries to find a game with a not found variant id
        // then: the response is a 401 with the proper problem
        client.post().uri("/games")
            .bodyValue(
                mapOf(
                    "id" to 100
                )
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: someone that is logged in tries to find a game with a not found variant id
        // then: the response is a 404 with the proper problem
        client.post().uri("/games")
            .bodyValue(
                mapOf(
                    "id" to 100
                )
            )
            .header("Authorization", "Bearer ${token1.token}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()

        // when: someone that is logged in tries to find a game with a found variant id
        // then: the response is a 201
        val userWaitingInLobby = client.post().uri("/games")
            .bodyValue(
                mapOf(
                    "id" to 17
                )
            )
            .header("Authorization", "Bearer ${token1.token}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(FindGameSuccess::class.java)
            .returnResult()
            .responseBody!!


        // when: a second user tries to find a game with a found variant id
        // then: the response is a 201
        val gameStarted = client.post().uri("/games")
            .bodyValue(
                mapOf(
                    "id" to 17
                )
            )
            .header("Authorization", "Bearer ${token2.token}")
            .exchange()
            .expectStatus().isCreated
            .expectBody(FindGameSuccess::class.java)
            .returnResult()
            .responseBody!!

        //when: trying to get a game that doesn't exist
        //then: the response is a 404 with the proper problem
        client.get().uri("/games/${gameStarted.id.value + 1}")
            .header("Authorization", "Bearer ${token1.token}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()

        //when: trying to get a game that exists
        //then: the response is a 200 with the proper representation
        val game = client.get().uri("/games/${gameStarted.id.value}")
            .header("Authorization", "Bearer ${token1.token}")
            .exchange()
            .expectStatus().isOk




    }

        private fun creatingUser(username: Username, password: Password, email: Email, client: WebTestClient): IdOutputModel {

            return client.post().uri("/users")
                .bodyValue(
                    mapOf(
                        "username" to username.value,
                        "password" to password.value,
                        "email" to email.value
                    )
                )
                .exchange()
                .expectStatus().isCreated
                .expectBody(IdOutputModel::class.java)
                .returnResult()
                .responseBody!!
        }

    private fun loggingUser(username: Username, password: Password, client: WebTestClient): UserTokenCreateOutputModel {

        return client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to username.value,
                    "password" to password.value
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(UserTokenCreateOutputModel::class.java)
            .returnResult()
            .responseBody!!


    }



}