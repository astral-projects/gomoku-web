package gomoku.http

import gomoku.TestDataGenerator.newTestEmail
import gomoku.TestDataGenerator.newTestPassword
import gomoku.TestDataGenerator.newTestUserName
import gomoku.domain.PaginatedResult
import gomoku.http.model.IdOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserOutputModel
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTests {

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `can create and retrieve user`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val username = newTestUserName()
        val password = newTestPassword()
        val email = newTestEmail()

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        val userId = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username.value,
                    "password" to password.value,
                    "email" to email.value
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

        // when: creating an user with the same username
        // then: the response is a 400 with a proper problem
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username.value,
                    "password" to password.value,
                    "email" to email.value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()

        // when: creating an user with the same email
        // then: the response is a 400 with a proper problem
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newTestUserName().value,
                    "password" to password.value,
                    "email" to email.value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()

        // when: getting the user
        // then: the response is a 200 with the proper representation
        val userOutputModel = client.get().uri("/users/${userId.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // and: the user has the same attributes
        assertEquals(username.value, userOutputModel.username)
        assertEquals(email.value, userOutputModel.email)
        assertEquals(userId.id, userOutputModel.id)

        // when: getting the user with an id of a non-existing user
        // then: the response is a 404
        client.get().uri("/users/${userId.id + 1}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()

        // when: getting the user with an invalid id
        // then: the response is a 400
        client.get().uri("/users/0")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
    }

    @Test
    fun `can login, access user home and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val username = newTestUserName()
        val password = newTestPassword()
        val email = newTestEmail()

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username.value,
                    "password" to password.value,
                    "email" to email.value
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/api/users/"))
            }

        // when: creating a token
        // then: the response is a 200
        val result = client.post().uri("/users/token")
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

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client.get().uri("/home")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("username").isEqualTo(username.value)

        // when: getting the user home with an invalid token
        // then: the response is a 401 with the proper problem
        client.get().uri("/home")
            .header("Authorization", "Bearer ${result.token}-invalid")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: revoking the token
        // then: response is a 200
        client.post().uri("/logout")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isOk

        // when: getting the user home with the revoked token
        // then: response is a 401
        client.get().uri("/home")
            .header("Authorization", "Bearer ${result.token}")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
    }

    @Test
    fun `can retrieve user ranking information`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a set random users
        val nrOfUsers = 15
        repeat(nrOfUsers) {
            val username = newTestUserName()
            val password = newTestPassword()
            val email = newTestEmail()
            client.post().uri("/users")
                .bodyValue(
                    mapOf(
                        "username" to username.value,
                        "password" to password.value,
                        "email" to email.value
                    )
                )
                .exchange()
                .expectStatus().isCreated
                .expectHeader().value("location") {
                    assertTrue(it.startsWith("/api/users/"))
                }
        }

        // when: getting the user ranking information with no offset or limit
        // then: the response is a 200 with the proper representation
        val result = client.get().uri("/users/ranking")
            .exchange()
            .expectStatus().isOk
            .expectBody(PaginatedResult::class.java)
            .returnResult()
            .responseBody!!

        // and: the result is correctly paginated
        assertEquals(1, result.currentPage)
        assertEquals(10, result.itemsPerPage)

        // when: getting the user ranking information with a offset and limit combination that do not exceed the total
        // number of users
        val offset = 2
        val limit = 5
        // then: the response is a 200 with the proper representation
        val resultB = client.get().uri("/users/ranking?offset=$offset&limit=$limit")
            .exchange()
            .expectStatus().isOk
            .expectBody(PaginatedResult::class.java)
            .returnResult()
            .responseBody!!

        // and: the result is correctly paginated
        assertEquals(offset / limit + 1, resultB.currentPage)
        assertEquals(limit, resultB.itemsPerPage)

        // when: getting the user ranking information with an invalid offset
        // then: the response is a 400
        client.get().uri("/users/ranking?offset=-1")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()

        // when: getting the user ranking information with an invalid limit
        // then: the response is a 400
        client.get().uri("/users/ranking?limit=0")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
    }
}
