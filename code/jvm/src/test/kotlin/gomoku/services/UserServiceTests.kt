package gomoku.services

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PositiveValue
import gomoku.domain.components.Term
import gomoku.domain.token.Sha256TokenEncoder
import gomoku.domain.user.UsersDomain
import gomoku.domain.user.UsersDomainConfig
import gomoku.domain.user.components.Username
import gomoku.repository.jdbi.JdbiTestConfiguration.jdbi
import gomoku.repository.jdbi.transaction.JdbiTransactionManager
import gomoku.services.user.GettingUserError
import gomoku.services.user.TokenCreationError
import gomoku.services.user.TokenRevocationError
import gomoku.services.user.UserCreationError
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.IntrusiveTests
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.Success
import gomoku.utils.TestClock
import gomoku.utils.TestConfiguration.NR_OF_TEST_ITERATIONS
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestId
import gomoku.utils.TestDataGenerator.newTestPassword
import gomoku.utils.TestDataGenerator.newTestString
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.TestDataGenerator.randomTo
import gomoku.utils.get
import org.junit.jupiter.api.RepeatedTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@RequiresDatabaseConnection
@IntrusiveTests
class UserServiceTests {

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `cannot retrieve a user by the wrong username or email`() {
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

        // when: creating a user with the same username
        val emailB = newTestEmail()
        val passwordB = newTestPassword()

        // then: the creation is not successful
        when (val userCreationResult = userService.createUser(username, emailB, passwordB)) {
            is Failure -> assertIs<UserCreationError.UsernameAlreadyExists>(userCreationResult.value)
            is Success -> fail("Unexpected $createUserResult")
        }

        // when: creating a user with the same email
        val usernameC = newTestUserName()
        val passwordC = newTestPassword()

        // then: the creation is not successful
        when (val userCreationResult = userService.createUser(usernameC, email, passwordC)) {
            is Failure -> assertIs<UserCreationError.EmailAlreadyExists>(userCreationResult.value)
            is Success -> fail("Unexpected $createUserResult")
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
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

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
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

        // when: searching for a user that does not exist
        val gettingUserResultB = userService.getUserById(newTestId())

        // then: the user is not found
        when (gettingUserResultB) {
            is Failure -> assertIs<GettingUserError.UserNotFound>(gettingUserResultB.value)
            is Success -> fail("Unexpected $gettingUserResultB")
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
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

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `cannot create token when user credentials are invalid`() {
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

        // when: creating a token with the wrong username
        val wrongUsername = newTestUserName()
        val createTokenResultB = userService.createToken(wrongUsername, password)

        // then: the creation is not successful
        when (createTokenResultB) {
            is Failure -> assertIs<TokenCreationError.UsernameNotExists>(createTokenResultB.value)
            is Success -> fail("Unexpected $createTokenResultB")
        }

        // when: creating a token with the wrong password
        val wrongPassword = newTestPassword()
        val createTokenResult = userService.createToken(username, wrongPassword)

        // then: the creation is not successful
        when (createTokenResult) {
            is Failure -> assertIs<TokenCreationError.PasswordIsWrong>(createTokenResult.value)
            is Success -> fail("Unexpected $createTokenResult")
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
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

        // when: retrieving the user by an invalid token
        // then: the user is not found
        assertNull(userService.getUserByToken("hardcoded-string"))

        // when: retrieving the user by the new token
        // then: the user is found
        assertNotNull(userService.getUserByToken(newToken.tokenValue))

        // and: the first token (the least recently used) is not valid
        assertNull(userService.getUserByToken(tokens[0].tokenValue))

        // and: the remaining tokens are still valid
        (1 until tokens.size).forEach {
            assertNotNull(userService.getUserByToken(tokens[it].tokenValue))
        }
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
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

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
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

        // when: revoking the token with an invalid token
        val invalidRevokeResult = userService.revokeToken("hardcoded-string")

        // then: the token is not revoked
        when (invalidRevokeResult) {
            is Failure -> assertIs<TokenRevocationError.TokenIsInvalid>(invalidRevokeResult.value)
            is Success -> fail("Unexpected $invalidRevokeResult")
        }

        // when: revoking the valid token
        userService.revokeToken(token.tokenValue)

        // and: retrieving the user by the revoked token
        maybeUser = userService.getUserByToken(token.tokenValue)

        // then: the token is not valid anymore
        assertNull(maybeUser)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can retrieve users ranking information`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating x users
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

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can retrieve a user stats info by id`() {
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
        val gettingUserResult = userService.getUserStats(userId)

        // then: the user is found
        when (gettingUserResult) {
            null -> fail("Unexpected $gettingUserResult")
            else -> assertEquals(gettingUserResult.id, userId)
        }

        // and: has the expected information
        assertEquals(username, gettingUserResult.username)
        assertEquals(email, gettingUserResult.email)
        assertEquals(0, gettingUserResult.points.value)
        assertEquals(0, gettingUserResult.gamesPlayed.value)
        assertEquals(0, gettingUserResult.wins.value)
        assertEquals(0, gettingUserResult.draws.value)
        assertEquals(0, gettingUserResult.losses.value)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can retrieve user stats info by username`() {
        // given: a user service
        val testClock = TestClock()
        val userService = createUsersService(testClock)

        // when: creating x users
        val nrOfUsers = 10 randomTo 41
        val usernameFormat = newTestString(minLength = Username.minLength, maxLength = Username.maxLength)
        repeat(nrOfUsers) {
            val username =
                if (it % 2 == 0) {
                    Username("$usernameFormat$it").get()
                } else {
                    newTestUserName()
                }
            val email = newTestEmail()
            val password = newTestPassword()
            val createUserResult = userService.createUser(username, email, password)

            // then: the creation is successful
            when (createUserResult) {
                is Failure -> fail("Unexpected $createUserResult")
                is Success -> assertTrue(createUserResult.value.value > 0)
            }
        }

        // when: retrieving users statistic information by username format defined above
        val limit = nrOfUsers
        val ranking = userService.getUserStatsByTerm(
            term = Term(usernameFormat).get(),
            limit = PositiveValue(limit).get(),
            offset = NonNegativeValue(0).get()
        )

        val actualUsers = if (nrOfUsers % 2 == 0) {
            nrOfUsers / 2
        } else {
            nrOfUsers / 2 + 1
        }

        // then: the statistics is paginated and the number of users is the expected one
        assertEquals(actualUsers, ranking.totalItems)
        assertEquals(actualUsers, ranking.itemsPerPage)

        // when: offset is not 0
        val offset = 2
        val rankingWithOffset = userService.getUserStatsByTerm(
            term = Term(usernameFormat).get(),
            offset = NonNegativeValue(offset).get(),
            limit = PositiveValue(limit).get()
        )

        // then: the statistics is paginated and first offset users are skipped
        assertEquals(actualUsers, rankingWithOffset.totalItems)
        assertEquals(actualUsers - offset, rankingWithOffset.itemsPerPage)

        // when: creating another user with a unique username
        val usernameToQuery = newTestUserName()
        val email = newTestEmail()
        val password = newTestPassword()
        val createUserResult = userService.createUser(usernameToQuery, email, password)

        // then: the creation is successful
        when (createUserResult) {
            is Failure -> fail("Unexpected $createUserResult")
            is Success -> assertTrue(createUserResult.value.value > 0)
        }

        // when: retrieving users statistic information by the previously created username
        val singleRanking = userService.getUserStatsByTerm(
            term = Term(usernameToQuery.value).get(),
            offset = NonNegativeValue(0).get(),
            limit = PositiveValue(limit).get()
        )

        // then: the statistics is paginated and the number of users is the expected one
        assertEquals(1, singleRanking.totalItems)
    }

    companion object {

        private fun createUsersService(
            testClock: TestClock,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3,
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
