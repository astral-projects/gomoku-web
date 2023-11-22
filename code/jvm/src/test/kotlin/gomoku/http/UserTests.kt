package gomoku.http

import gomoku.domain.PaginatedResult
import gomoku.domain.components.Term
import gomoku.http.media.Problem
import gomoku.http.model.IdOutputModel
import gomoku.http.model.token.UserTokenCreateOutputModel
import gomoku.http.model.user.UserLogoutOutputModel
import gomoku.http.model.user.UserOutputModel
import gomoku.http.model.user.UserStatsOutputModel
import gomoku.http.utils.HttpTestAssistant.createRandomUser
import gomoku.http.utils.HttpTestAssistant.createToken
import gomoku.utils.IntrusiveTests
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestId
import gomoku.utils.TestDataGenerator.newTestPassword
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.TestDataGenerator.randomTo
import gomoku.utils.get
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RequiresDatabaseConnection
@IntrusiveTests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTests {

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `can create an user`() {
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
                assertTrue(it.startsWith("/api/users"))
            }
            .expectBody(IdOutputModel::class.java)
            .returnResult()
            .responseBody!!

        assertTrue(userId.id > 0)

        // when: creating a user with the same username
        // then: the response is a 400 with a proper problem
        val sameUsernameProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to username.value,
                    "password" to password.value,
                    "email" to email.value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.usernameAlreadyExists, sameUsernameProblem.type)
        assertEquals("The username <$username> already exists", sameUsernameProblem.detail)
        val sameUsernameInstance = assertNotNull(sameUsernameProblem.instance)
        assertEquals(URI("/api/users"), sameUsernameInstance)
        assertEquals("Username already exists", sameUsernameProblem.title)
        assertEquals(400, sameUsernameProblem.status)

        // when: creating a user with the same email
        // then: the response is a 400 with a proper problem
        val sameEmailProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newTestUserName().value,
                    "password" to password.value,
                    "email" to email.value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.emailAlreadyExists, sameEmailProblem.type)
        assertEquals("The email <${email.value}> already exists", sameEmailProblem.detail)
        val sameEmailInstance = assertNotNull(sameEmailProblem.instance)
        assertEquals(URI("/api/users"), sameEmailInstance)
        assertEquals("Email already exists", sameEmailProblem.title)
        assertEquals(400, sameEmailProblem.status)

        // when: creating a user with an invalid email
        // then: the response is a 400 with a proper problem
        val invalidEmailProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newTestUserName().value,
                    "password" to password.value,
                    "email" to "not a valid email"
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidEmail, invalidEmailProblem.type)
        assertEquals("The email is invalid because it does not match the email format", invalidEmailProblem.detail)
        val invalidEmailInstance = assertNotNull(invalidEmailProblem.instance)
        assertEquals(URI("/api/users"), invalidEmailInstance)
        assertEquals("Invalid email", invalidEmailProblem.title)
        assertEquals(400, invalidEmailProblem.status)

        // when: creating a user with a blank username
        // then: the response is a 400 with a proper problem
        val blankUsernameProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to "  ",
                    "password" to password.value,
                    "email" to newTestEmail().value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.blankUsername, blankUsernameProblem.type)
        assertEquals("The username cannot be blank", blankUsernameProblem.detail)
        val blankUsernameInstance = assertNotNull(blankUsernameProblem.instance)
        assertEquals(URI("/api/users"), blankUsernameInstance)
        assertEquals("Blank username", blankUsernameProblem.title)
        assertEquals(400, blankUsernameProblem.status)

        // when: creating a user with a username that is too short
        // then: the response is a 400 with a proper problem
        val shortUsernameProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to "adad",
                    "password" to password.value,
                    "email" to newTestEmail().value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidUsernameLength, shortUsernameProblem.type)
        assertEquals("Username must be between 5 and 30 characters", shortUsernameProblem.detail)
        val shortUsernameInstance = assertNotNull(shortUsernameProblem.instance)
        assertEquals(URI("/api/users"), shortUsernameInstance)
        assertEquals("Invalid username length", shortUsernameProblem.title)
        assertEquals(400, shortUsernameProblem.status)

        // when creating a user with a username that is too long
        // then: the response is a 400 with a proper problem
        val longUsernameProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to "a".repeat(100),
                    "password" to password.value,
                    "email" to newTestEmail().value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidUsernameLength, longUsernameProblem.type)
        assertEquals("Username must be between 5 and 30 characters", longUsernameProblem.detail)
        val longUsernameInstance = assertNotNull(longUsernameProblem.instance)
        assertEquals(URI("/api/users"), longUsernameInstance)
        assertEquals("Invalid username length", longUsernameProblem.title)
        assertEquals(400, longUsernameProblem.status)

        // when: creating a user with a blank password
        // then: the response is a 400 with a proper problem
        val blankPasswordProblem: Problem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newTestUserName().value,
                    "password" to "   ",
                    "email" to newTestEmail().value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.blankPassword, blankPasswordProblem.type)
        assertEquals("The password cannot be blank", blankPasswordProblem.detail)
        val blankPasswordInstance = assertNotNull(blankPasswordProblem.instance)
        assertEquals(URI("/api/users"), blankPasswordInstance)
        assertEquals("Blank password", blankPasswordProblem.title)
        assertEquals(400, blankPasswordProblem.status)

        // when: creating a user with a password that is too short
        // then: the response is a 400 with a proper problem
        val shortPasswordProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newTestUserName().value,
                    "password" to "ab",
                    "email" to newTestEmail().value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.insecurePassword, shortPasswordProblem.type)
        assertEquals("Password must be between 8 and 40 characters", shortPasswordProblem.detail)
        val shortPasswordInstance = assertNotNull(shortPasswordProblem.instance)
        assertEquals(URI("/api/users"), shortPasswordInstance)
        assertEquals("Password not safe", shortPasswordProblem.title)
        assertEquals(400, shortPasswordProblem.status)

        // when: creating a user with a password that is too long
        // then: the response is a 400 with a proper problem
        val longPasswordProblem = client.post().uri("/users")
            .bodyValue(
                mapOf(
                    "username" to newTestUserName().value,
                    "password" to "b".repeat(100),
                    "email" to newTestEmail().value
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.insecurePassword, longPasswordProblem.type)
        assertEquals("Password must be between 8 and 40 characters", shortPasswordProblem.detail)
        val longPasswordInstance = assertNotNull(longPasswordProblem.instance)
        assertEquals(URI("/api/users"), longPasswordInstance)
        assertEquals("Password not safe", longPasswordProblem.title)
        assertEquals(400, longPasswordProblem.status)
    }

    @Test
    fun `can login`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val (_, registrationCredentials) = createRandomUser(client)

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

        // when: creating a token with an invalid username
        // then: the response is a 401 with a proper problem
        val invalidUsername = "invalid-username"
        val invalidUsernameProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to invalidUsername,
                    "password" to registrationCredentials.password
                )
            )
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.usernameDoesNotExists, invalidUsernameProblem.type)
        assertEquals("The user with username <$invalidUsername> does not exist", invalidUsernameProblem.detail)
        val invalidUsernameInstance = assertNotNull(invalidUsernameProblem.instance)
        assertEquals(URI("/api/users/token"), invalidUsernameInstance)
        assertEquals("Username doesn't exist", invalidUsernameProblem.title)
        assertEquals(404, invalidUsernameProblem.status)

        // when: creating a token with an invalid password
        // then: the response is a 401 with a proper problem
        val wrongPasswordProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to registrationCredentials.username,
                    "password" to "wrong-password"
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidPassword, wrongPasswordProblem.type)
        assertEquals("The password received does not match the user's username", wrongPasswordProblem.detail)
        val invalidPasswordInstance = assertNotNull(wrongPasswordProblem.instance)
        assertEquals(URI("/api/users/token"), invalidPasswordInstance)
        assertEquals("Wrong password", wrongPasswordProblem.title)
        assertEquals(400, wrongPasswordProblem.status)

        // when: creating a token with a blank username
        // then: the response is a 400 with a proper problem
        val blankUsernameProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to "  ",
                    "password" to registrationCredentials.password
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.blankUsername, blankUsernameProblem.type)
        assertEquals("The username cannot be blank", blankUsernameProblem.detail)
        val blankUsernameInstance = assertNotNull(blankUsernameProblem.instance)
        assertEquals(URI("/api/users/token"), blankUsernameInstance)
        assertEquals("Blank username", blankUsernameProblem.title)
        assertEquals(400, blankUsernameProblem.status)

        // when: creating a token with a username that is too short
        // then: the response is a 400 with a proper problem
        val shortUsernameProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to "adad",
                    "password" to registrationCredentials.password
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidUsernameLength, shortUsernameProblem.type)
        assertEquals("Username must be between 5 and 30 characters", shortUsernameProblem.detail)
        val shortUsernameInstance = assertNotNull(shortUsernameProblem.instance)
        assertEquals(URI("/api/users/token"), shortUsernameInstance)
        assertEquals("Invalid username length", shortUsernameProblem.title)
        assertEquals(400, shortUsernameProblem.status)

        // when: creating a token with a username that is too long
        // then: the response is a 400 with a proper problem
        val longUsernameProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to "a".repeat(100),
                    "password" to registrationCredentials.password
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidUsernameLength, longUsernameProblem.type)
        assertEquals("Username must be between 5 and 30 characters", longUsernameProblem.detail)
        val longUsernameInstance = assertNotNull(longUsernameProblem.instance)
        assertEquals(URI("/api/users/token"), longUsernameInstance)
        assertEquals("Invalid username length", longUsernameProblem.title)
        assertEquals(400, longUsernameProblem.status)

        // when: creating a token with a blank password
        // then: the response is a 400 with a proper problem
        val blankPasswordProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to registrationCredentials.username,
                    "password" to "   "
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.blankPassword, blankPasswordProblem.type)
        assertEquals("The password cannot be blank", blankPasswordProblem.detail)
        val blankPasswordInstance = assertNotNull(blankPasswordProblem.instance)
        assertEquals(URI("/api/users/token"), blankPasswordInstance)
        assertEquals("Blank password", blankPasswordProblem.title)
        assertEquals(400, blankPasswordProblem.status)

        // when: creating a token with a password that is not safe
        // then: the response is a 400 with a proper problem
        val insecurePasswordProblem = client.post().uri("/users/token")
            .bodyValue(
                mapOf(
                    "username" to registrationCredentials.username,
                    "password" to "1234567"
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.insecurePassword, insecurePasswordProblem.type)
        assertEquals("Password must be between 8 and 40 characters", insecurePasswordProblem.detail)
        val insecurePasswordInstance = assertNotNull(insecurePasswordProblem.instance)
        assertEquals(URI("/api/users/token"), insecurePasswordInstance)
        assertEquals("Password not safe", insecurePasswordProblem.title)
        assertEquals(400, insecurePasswordProblem.status)
    }

    @Test
    fun `can access user home and logout`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val (_, registrationCredentials) = createRandomUser(client)

        // and: a token
        val token = createToken(client, registrationCredentials).token

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client.get().uri("/users/home")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody()

        // when: getting the user home with an invalid token
        // then: the response is a 401 with the proper problem
        client.get().uri("/users/home")
            .header("Authorization", "Bearer invalid-token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")

        // when: revoking the token
        // then: response is a 200 with the proper representation
        val userLogoutOutputModel = client.post().uri("/users/logout")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserLogoutOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // and: the token is revoked with the proper message
        assertEquals("User logged out successfully, token was revoked.", userLogoutOutputModel.message)

        // when: accessing the user home with the revoked token
        // then: the response is a 401 with the proper problem
        client.get().uri("/users/home")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()

        // when: accessing logout without authentication
        // then: the response is a 401 with the proper problem
        client.post().uri("/users/logout")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
    }

    @Test
    fun `can retrieve an user by id`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val (userId, registrationCredentials) = createRandomUser(client)

        // when: getting the user
        // then: the response is a 200 with the proper representation
        val userOutputModel = client.get().uri("/users/$userId")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // and: the user is the same as the one created
        assertEquals(userId, userOutputModel.id)
        assertEquals(registrationCredentials.username, userOutputModel.username)
        assertEquals(registrationCredentials.email, userOutputModel.email)

        // when: getting a user with an invalid id
        // then: the response is a 404 with the proper problem
        val invalidId = -1
        val invalidIdProblem = client.get().uri("/users/$invalidId")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidIdProblem.type)
        assertEquals("The user id must be a positive integer", invalidIdProblem.detail)
        val invalidIdInstance = assertNotNull(invalidIdProblem.instance)
        assertEquals(URI("/api/users/$invalidId"), invalidIdInstance)
        assertEquals("Invalid user id", invalidIdProblem.title)
        assertEquals(404, invalidIdProblem.status)

        // when: getting a user that does not exist
        // then: the response is a 404 with the proper problem
        val notFoundId = newTestId()
        val notFoundUserProblem = client.get().uri("/users/${notFoundId.value}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userNotFound, notFoundUserProblem.type)
        assertEquals("The user with id <${notFoundId.value}> was not found", notFoundUserProblem.detail)
        val notFoundUserInstance = assertNotNull(notFoundUserProblem.instance)
        assertEquals(URI("/api/users/${notFoundId.value}"), notFoundUserInstance)
        assertEquals("User not found", notFoundUserProblem.title)
        assertEquals(404, notFoundUserProblem.status)
    }

    @Test
    fun `can retrieve users statistic information`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a set random users
        val nrOfUsers = 15 randomTo 30
        repeat(nrOfUsers) {
            createRandomUser(client)
        }

        // when: getting the user statistic information with no offset or limit
        // then: the response is a 200 with the proper representation
        val resultWithNoOffsetOrLimit = client.get().uri("/users/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody(PaginatedResult::class.java)
            .returnResult()
            .responseBody!!

        // and: the result is correctly paginated
        assertEquals(1, resultWithNoOffsetOrLimit.currentPage)
        assertEquals(10, resultWithNoOffsetOrLimit.itemsPerPage)

        // when: getting the user statistic information
        // with an offset and limit combination that do not exceed the total number of users created earlier
        val offset = 2
        val limit = 5

        // then: the response is a 200 with the proper representation
        val resultWithOffsetAndLimit = client.get().uri("/users/stats?offset=$offset&limit=$limit")
            .exchange()
            .expectStatus().isOk
            .expectBody(PaginatedResult::class.java)
            .returnResult()
            .responseBody!!

        // and: the result is correctly paginated
        assertEquals(offset / limit + 1, resultWithOffsetAndLimit.currentPage)
        assertEquals(if (limit < nrOfUsers) limit else nrOfUsers, resultWithOffsetAndLimit.itemsPerPage)

        // when: getting the user statistic information with an invalid offset
        // then: the response is a 400
        val invalidOffset = -1
        val invalidOffsetProblem = client.get().uri("/users/stats?offset=$invalidOffset")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidOffset, invalidOffsetProblem.type)
        assertEquals("The offset must be a non-negative integer", invalidOffsetProblem.detail)
        val invalidOffsetInstance = assertNotNull(invalidOffsetProblem.instance)
        assertEquals(URI("/api/users/stats?offset=$invalidOffset&limit=10"), invalidOffsetInstance)
        assertEquals("Invalid offset", invalidOffsetProblem.title)
        assertEquals(400, invalidOffsetProblem.status)

        // when: getting the user statistic information with an invalid limit
        // then: the response is a 400 with a proper problem
        val invalidLimit = 0
        val invalidLimitProblem = client.get().uri("/users/stats?limit=$invalidLimit")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidLimit, invalidLimitProblem.type)
        assertEquals("The limit must be a positive integer", invalidLimitProblem.detail)
        val invalidLimitInstance = assertNotNull(invalidLimitProblem.instance)
        assertEquals(URI("/api/users/stats?offset=0&limit=$invalidLimit"), invalidLimitInstance)
        assertEquals("Invalid limit", invalidLimitProblem.title)
        assertEquals(400, invalidLimitProblem.status)
    }

    @Test
    fun `can retrieve user statistic information`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a random user
        val (userId, registrationCredentials) = createRandomUser(client)

        // when: getting the user statistic information
        // then: the response is a 200 with the proper representation
        val userStatsOutputModel = client.get().uri("/users/$userId/stats")
            .exchange()
            .expectStatus().isOk
            .expectBody(UserStatsOutputModel::class.java)
            .returnResult()
            .responseBody!!

        // and: the user is the same as the one created
        assertEquals(userId, userStatsOutputModel.id)
        assertEquals(registrationCredentials.username, userStatsOutputModel.username)
        assertEquals(registrationCredentials.email, userStatsOutputModel.email)

        // and: the user has no games played yet
        assertEquals(0, userStatsOutputModel.gamesPlayed)
        assertEquals(0, userStatsOutputModel.wins)
        assertEquals(0, userStatsOutputModel.losses)
        assertEquals(0, userStatsOutputModel.draws)
        assertEquals(0, userStatsOutputModel.points)

        // when: getting the user statistic information with an invalid id
        // then: the response is a 404 with the proper problem
        val invalidId = -1
        val invalidIdProblem = client.get().uri("/users/$invalidId/stats")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidId, invalidIdProblem.type)
        assertEquals("The user id must be a positive integer", invalidIdProblem.detail)
        val invalidIdInstance = assertNotNull(invalidIdProblem.instance)
        assertEquals(URI("/api/users/$invalidId/stats"), invalidIdInstance)
        assertEquals("Invalid user id", invalidIdProblem.title)
        assertEquals(404, invalidIdProblem.status)

        // when: getting a user that does not exist
        // then: the response is a 404 with the proper problem
        val notFoundId = newTestId()
        val notFoundUserProblem = client.get().uri("/users/${notFoundId.value}/stats")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.userNotFound, notFoundUserProblem.type)
        assertEquals("The user with id <${notFoundId.value}> was not found", notFoundUserProblem.detail)
        val notFoundUserInstance = assertNotNull(notFoundUserProblem.instance)
        assertEquals(URI("/api/users/${notFoundId.value}/stats"), notFoundUserInstance)
        assertEquals("User not found", notFoundUserProblem.title)
        assertEquals(404, notFoundUserProblem.status)
    }

    @Test
    fun `can retrieve users statistic information by search term`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        // and: a valid term
        val usernameSuffix = "suffix"
        val term = Term(usernameSuffix).get().value

        // and: a set random users with the same suffix
        val nrOfUsers = 15 randomTo 30
        repeat(nrOfUsers) {
            if (it % 2 == 0) {
                createRandomUser(client)
            } else {
                createRandomUser(client, usernameSuffix)
            }
        }

        // and: a logged-in user
        val (_, registrationCredentials) = createRandomUser(client)
        val token = createToken(client, registrationCredentials).token

        // when: getting the user statistic information by search term with no offset or limit
        // then: the response is a 200 with the proper representation
        val resultWithNoOffsetOrLimit = client.get().uri("/users/stats/search?term=$term")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(PaginatedResult::class.java)
            .returnResult()
            .responseBody!!

        // and: the result is correctly paginated
        assertEquals(1, resultWithNoOffsetOrLimit.currentPage)
        assertEquals(10, resultWithNoOffsetOrLimit.itemsPerPage)

        // when: getting the user statistic information by search term with an offset and
        // limit combination that do not exceed the total number of users created earlier
        val offset = 2
        val limit = 5

        // then: the response is a 200 with the proper representation
        val resultWithOffsetAndLimit = client.get().uri("/users/stats/search?term=$term&offset=$offset&limit=$limit")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody(PaginatedResult::class.java)
            .returnResult()
            .responseBody!!

        // and: the result is correctly paginated
        assertEquals(offset / limit + 1, resultWithOffsetAndLimit.currentPage)
        assertEquals(if (limit < nrOfUsers) limit else nrOfUsers, resultWithOffsetAndLimit.itemsPerPage)

        // when: getting the user statistic information by search term with an invalid offset
        // then: the response is a 400
        val invalidOffset = -1
        val invalidOffsetProblem = client.get().uri("/users/stats/search?term=$term&offset=$invalidOffset")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidOffset, invalidOffsetProblem.type)
        assertEquals("The offset must be a non-negative integer", invalidOffsetProblem.detail)
        val invalidOffsetInstance = assertNotNull(invalidOffsetProblem.instance)
        assertEquals(URI("/api/users/stats/search?term=$term&offset=$invalidOffset&limit=10"), invalidOffsetInstance)
        assertEquals("Invalid offset", invalidOffsetProblem.title)
        assertEquals(400, invalidOffsetProblem.status)

        // when: getting the user statistic information by search term with an invalid limit
        // then: the response is a 400 with a proper problem
        val invalidLimit = 0
        val invalidLimitProblem = client.get().uri("/users/stats/search?term=$term&limit=$invalidLimit")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidLimit, invalidLimitProblem.type)
        assertEquals("The limit must be a positive integer", invalidLimitProblem.detail)
        val invalidLimitInstance = assertNotNull(invalidLimitProblem.instance)
        assertEquals(URI("/api/users/stats/search?term=$term&offset=0&limit=$invalidLimit"), invalidLimitInstance)
        assertEquals("Invalid limit", invalidLimitProblem.title)
        assertEquals(400, invalidLimitProblem.status)

        // when: getting the user statistic information by a search term that is too short
        val shortSearchTerm = "a"
        assertThrows<IllegalArgumentException> {
            Term(shortSearchTerm).get().value
        }

        // then: the response is a 400 with a proper problem
        val shortSearchTermProblem = client.get().uri("/users/stats/search?term=$shortSearchTerm")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Problem::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(Problem.invalidTermLength, shortSearchTermProblem.type)
        assertEquals("The search term must be above 3 characters", shortSearchTermProblem.detail)
        val shortSearchTermInstance = assertNotNull(shortSearchTermProblem.instance)
        assertEquals(URI("/api/users/stats/search?term=$shortSearchTerm&offset=0&limit=10"), shortSearchTermInstance)
        assertEquals("Invalid search term length", shortSearchTermProblem.title)
        assertEquals(400, shortSearchTermProblem.status)

        // when: getting the user statistic information without authentication
        // then: the response is a 401 with the proper problem
        client.get().uri("/users/stats/search?term=$term")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
    }
}
