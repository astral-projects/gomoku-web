package gomoku.services

import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.token.Sha256TokenEncoder
import gomoku.domain.user.UsersDomain
import gomoku.domain.user.UsersDomainConfig
import gomoku.repository.jdbi.JdbiTestConfiguration.jdbi
import gomoku.repository.jdbi.transaction.JdbiTransactionManager
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.TestClock
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestPassword
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.get
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UserServiceTests {

    @Test
    fun `can create user, token, and retrieve by token`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(username, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // when: creating a token
        val createTokenResult = userService.createToken(username, password)

        // then: the creation is successful
        val token = when (createTokenResult) {
            is Failure -> fail(createTokenResult.toString())
            is Success -> createTokenResult.value.tokenValue
        }

        // and: the token bytes have the expected length
        val tokenBytes = Base64.getUrlDecoder().decode(token)
        assertEquals(256 / 8, tokenBytes.size)

        // when: retrieving the user by token
        val user = userService.getUserByToken(token)

        // then: a user is found
        assertNotNull(user)

        // and: has the expected information
        assertEquals(username, user.username)
        assertEquals(email, user.email)
    }

    @Test
    fun `can retrieve user by id`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating a user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(username, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // and: retrieving the user by id
        val userId = createUserResult.value
        val gettingUserResult = userService.getUserById(userId)

        // then: the user is found
        when (gettingUserResult) {
            is Failure -> fail("Unexpected $gettingUserResult")
            is Success -> assertEquals(gettingUserResult.value.id, userId)
        }

        // and: has the expected information
        val user = gettingUserResult.value
        assertEquals(username, user.username)
        assertEquals(email, user.email)
    }

    @Test
    fun `can use token during rolling period but not after absolute TTL`() {
        // given: a user service
        val testClock = TestClock()
        val tokenTtl = 90.minutes
        val tokenRollingTtl = 30.minutes
        val userService = createUsersService(testClock, tokenTtl, tokenRollingTtl)

        // when: creating a user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(username, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // when: creating a token
        val createTokenResult = userService.createToken(username, password)

        // then: the creation is successful
        val token = when (createTokenResult) {
            is Failure -> fail(createTokenResult.toString())
            is Success -> createTokenResult.value.tokenValue
        }

        // when: retrieving the user after (rolling TTL - 1s) intervals
        val startInstant = testClock.now()
        while (true) {
            testClock.advance(tokenRollingTtl.minus(1.seconds))
            userService.getUserByToken(token) ?: break
        }

        // then: user is not found only after the absolute TTL has elapsed
        assertTrue((testClock.now() - startInstant) > tokenTtl)
    }

    @Test
    fun `can limit the number of tokens`() {
        // given: a user service
        val testClock = TestClock()
        val maxTokensPerUser = 5
        val userService = createUsersService(testClock, maxTokensPerUser = maxTokensPerUser)

        // when: creating a user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(username, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // when: creating MAX tokens
        val tokens = (0 until maxTokensPerUser).map {
            val createTokenResult = userService.createToken(username, password)
            testClock.advance(1.minutes)

            // then: the creation is successful
            val token = when (createTokenResult) {
                is Failure -> fail(createTokenResult.toString())
                is Success -> createTokenResult.value
            }
            token
        }.toTypedArray().reversedArray()

        // and: using the tokens at different times
        (tokens.indices).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue), "token $it must be valid")
            testClock.advance(1.seconds)
        }

        // and: creating a new token
        val createTokenResult = userService.createToken(username, password)
        testClock.advance(1.seconds)
        val newToken = when (createTokenResult) {
            is Failure -> fail(createTokenResult.toString())
            is Success -> createTokenResult.value
        }

        // then: newToken is valid
        assertNotNull(userService.getUserByToken(newToken.tokenValue))

        // and: the first token (the least recently used) is not valid
        assertNull(userService.getUserByToken(tokens[0].tokenValue))

        // and: the remaining tokens are still valid
        (1 until tokens.size).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue))
        }
    }

    @Test
    fun `can limit the number of tokens even if multiple tokens are used at the same time`() {
        // given: a user service
        val testClock = TestClock()
        val maxTokensPerUser = 5
        val userService = createUsersService(testClock, maxTokensPerUser = maxTokensPerUser)

        // when: creating a user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(username, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // when: creating MAX tokens
        val tokens = (0 until maxTokensPerUser).map {
            val createTokenResult = userService.createToken(username, password)
            testClock.advance(1.minutes)

            // then: the creation is successful
            val token = when (createTokenResult) {
                is Failure -> fail(createTokenResult.toString())
                is Success -> createTokenResult.value
            }
            token
        }.toTypedArray().reversedArray()

        // and: using the tokens at the same time
        testClock.advance(1.minutes)
        (tokens.indices).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue), "token $it must be valid")
        }

        // and: creating a new token
        val createTokenResult = userService.createToken(username, password)
        testClock.advance(1.minutes)
        val newToken = when (createTokenResult) {
            is Failure -> fail(createTokenResult.toString())
            is Success -> createTokenResult.value
        }

        // then: newToken is valid
        assertNotNull(userService.getUserByToken(newToken.tokenValue))

        // and: exactly one of the previous tokens is now not valid
        assertEquals(
            maxTokensPerUser - 1,
            tokens.count {
                userService.getUserByToken(it.tokenValue) != null
            }
        )
    }

    @Test
    fun `can logout`() {
        // given: a user service
        val testClock = TestClock()
        val maxTokensPerUser = 5
        val userService = createUsersService(testClock, maxTokensPerUser = maxTokensPerUser)

        // when: creating a user
        val username = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(username, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // when: creating a token
        val tokenCreationResult = userService.createToken(username, password)

        // then: token creation is successful
        val token = when (tokenCreationResult) {
            is Failure -> fail("Token creation should be successful: '${tokenCreationResult.value}'")
            is Success -> tokenCreationResult.value
        }

        // when: using the token
        var maybeUser = userService.getUserByToken(token.tokenValue)

        // then: token usage is successful
        assertNotNull(maybeUser)

        // when: revoking the token
        userService.revokeToken(token.tokenValue)

        // and: retrieving the user by the revoked token
        maybeUser = userService.getUserByToken(token.tokenValue)

        // then: the token is not valid anymore
        assertNull(maybeUser)
    }

    @Test
    fun `can retrieve users ranking information`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating 10 users
        val nrOfUsers = 10
        repeat(nrOfUsers) {
            val username = newTestUserName()
            val email = newTestEmail()
            val password = newTestPassword()
            val createUserResult = userService.createUser(username, email, password)

            // then: the creation is successful
            when (createUserResult) {
                is Failure -> fail("Unexpected $createUserResult")
                is Success -> assertTrue(createUserResult.value.value > 0)
            }
        }

        // when: retrieving the users statistic information
        val limit = nrOfUsers
        val ranking = userService.getUsersStats(
            offset = NonNegativeValue(0).get(),
            limit = PositiveValue(limit).get()
        )

        // then: the statistics is paginated
        assertEquals(nrOfUsers, ranking.items.size)
        assertEquals(limit, ranking.itemsPerPage)
    }

    companion object {

        private fun createUsersService(
            testClock: TestClock,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3
        ) = UsersService(
            JdbiTransactionManager(jdbi),
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = PositiveValue(256 / 8).get(),
                    tokenTtl = tokenTtl,
                    tokenRollingTtl = tokenRollingTtl,
                    maxTokensPerUser = PositiveValue(maxTokensPerUser).get()
                )
            ),
            testClock
        )
    }
}
