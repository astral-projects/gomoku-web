package gomoku.repository.jdbi

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PositiveValue
import gomoku.domain.components.Term
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.components.Username
import gomoku.repository.jdbi.JdbiTestConfiguration.runWithHandleAndRollback
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.TestClock
import gomoku.utils.TestConfiguration.NR_OF_TEST_ITERATIONS
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.TestDataGenerator.newTokenValidationData
import gomoku.utils.TestDataGenerator.randomTo
import gomoku.utils.get
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.RepeatedTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@RequiresDatabaseConnection
class JdbiUserRepositoryTests {

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can create and retrieve user`() = runWithHandleAndRollback { handle ->
        // given: a UsersRepository
        val repo = JdbiUsersRepository(handle)

        // when: storing a user
        val userName = newTestUserName()
        val email = newTestEmail()
        val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
        val createdUserId = repo.storeUser(userName, email, passwordValidationInfo)

        // and: retrieving the user by id
        val retrievedUserById: User? = repo.getUserById(createdUserId)

        // then:
        assertNotNull(retrievedUserById)
        assertEquals(createdUserId, retrievedUserById.id)

        // and: retrieving a user by username
        val retrievedUserByUsername: User? = repo.getUserByUsername(userName)

        // then:
        assertNotNull(retrievedUserByUsername)
        assertEquals(userName, retrievedUserByUsername.username)
        assertEquals(passwordValidationInfo, retrievedUserByUsername.passwordValidation)
        assertTrue(retrievedUserByUsername.id.value >= 0)

        // when: asking if the user exists by email
        val isUserIsStoredByEmail = repo.isUserStoredByEmail(email)

        // then: response is true
        assertTrue(isUserIsStoredByEmail)

        // when: asking if a user exists by email that does not exist
        val anotherUserIsStoredByEmail = repo.isUserStoredByEmail(newTestEmail())

        // then: response is false
        assertFalse(anotherUserIsStoredByEmail)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can create, validate and update tokens`() = runWithHandleAndRollback { handle ->
        // given: a UsersRepository
        val repo = JdbiUsersRepository(handle)
        // and: a test clock
        val clock = TestClock()

        // and: a createdUser
        val userName = newTestUserName()
        val email = newTestEmail()
        val passwordValidationInfo = PasswordValidationInfo("not-valid")
        val userId = repo.storeUser(userName, email, passwordValidationInfo)

        // and: test TokenValidationInfo
        val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

        // when: creating a token
        val tokenCreationInstant = clock.now()
        val token = Token(
            tokenValidationInfo = testTokenValidationInfo,
            userId = userId,
            createdAt = tokenCreationInstant,
            lastUsedAt = tokenCreationInstant
        )
        repo.createToken(token, PositiveValue(1).get())

        // then: createToken does not throw errors
        // no exception

        // when: retrieving the token and associated user
        val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

        // then:
        val (user, retrievedToken) = userAndToken ?: fail("token and associated user must exist")

        // and: the user is the same
        assertEquals(userName.value, user.username.value)
        assertEquals(testTokenValidationInfo.validationInfo, retrievedToken.tokenValidationInfo.validationInfo)
        assertEquals(tokenCreationInstant, retrievedToken.createdAt)

        // when: updating the token last used
        val tokenLastUsedInstant = clock.now()
        repo.updateTokenLastUsed(retrievedToken, tokenLastUsedInstant)

        // then: the token last used is updated
        val userAndTokenAfterUpdate = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)
        val (_, retrievedTokenAfterUpdate) = userAndTokenAfterUpdate ?: fail("token and associated user must exist")
        assertEquals(tokenLastUsedInstant, retrievedTokenAfterUpdate.lastUsedAt)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can revoke tokens`() = runWithHandleAndRollback { handle ->
        // given: a UsersRepository
        val repo = JdbiUsersRepository(handle)
        // and: a test clock
        val clock = TestClock()

        // and: a createdUser
        val userName = newTestUserName()
        val email = newTestEmail()
        val passwordValidationInfo = PasswordValidationInfo("not-valid")
        val userId = repo.storeUser(userName, email, passwordValidationInfo)

        // and: test TokenValidationInfo
        val testTokenValidationInfo = TokenValidationInfo(newTokenValidationData())

        // when: creating a token
        val tokenCreationInstant = clock.now()
        val token = Token(
            tokenValidationInfo = testTokenValidationInfo,
            userId = userId,
            createdAt = tokenCreationInstant,
            lastUsedAt = tokenCreationInstant
        )
        repo.createToken(token, PositiveValue(1).get())

        // when: retrieving the token and associated user
        val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

        // then:
        val (_, retrievedToken) = userAndToken ?: fail("token and associated user must exist")

        // when: when token is revoked
        repo.revokeToken(retrievedToken.tokenValidationInfo)

        // then: token is not found
        val userAndTokenAfterRevoke = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)
        assertNull(userAndTokenAfterRevoke)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can retrieve user statistic information`() = runWithHandleAndRollback { handle ->
        // given: a UsersRepository
        val repo = JdbiUsersRepository(handle)

        // when: storing a user
        val userName = newTestUserName()
        val email = newTestEmail()
        val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
        val userId = repo.storeUser(userName, email, passwordValidationInfo)

        // and: retrieving the user statistic information
        val userRankingInfo = repo.getUserStats(userId)

        // then: the user statistic information is correct
        assertNotNull(userRankingInfo)
        assertEquals(userName, userRankingInfo.username)
        assertEquals(email, userRankingInfo.email)
        assertEquals(0, userRankingInfo.gamesPlayed.value)
        assertEquals(0, userRankingInfo.wins.value)
        assertEquals(0, userRankingInfo.losses.value)
        assertEquals(0, userRankingInfo.points.value)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `retrieves all users paginated statistic information`() = runWithHandleAndRollback { handle ->
        // given: a UsersRepository
        val repo = JdbiUsersRepository(handle)

        // when: storing several users
        val nrOfUsers = 10
        repeat(nrOfUsers) {
            val userName = newTestUserName()
            val email = newTestEmail()
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(userName, email, passwordValidationInfo)
        }

        // and: retrieving the first 3 users statistic information
        val limitValue = 3
        val usersRanking = repo.getUsersStats(
            offset = NonNegativeValue(0).get(),
            limit = PositiveValue(limitValue).get()
        )

        // then: the users statistic information is paginated
        assertEquals(1, usersRanking.currentPage)
        assertEquals(limitValue, usersRanking.itemsPerPage)

        // when: comparing with user information
        // then: the users statistic information is correct
        repeat(3) {
            val retrievedUserByUsername: User? = repo.getUserByUsername(usersRanking.items[it].username)
            assertNotNull(retrievedUserByUsername)
            assertEquals(usersRanking.items[it].username, retrievedUserByUsername.username)
            assertEquals(usersRanking.items[it].email, retrievedUserByUsername.email)
        }

        // when: retrieving all users statistic information
        val secondLimitValue = nrOfUsers
        val allUsersRanking = repo.getUsersStats(
            offset = NonNegativeValue(0).get(),
            limit = PositiveValue(secondLimitValue).get()
        )

        // then: the users statistic is paginated
        assertEquals(1, allUsersRanking.currentPage)
        assertEquals(secondLimitValue, allUsersRanking.itemsPerPage)

        // when: when retrieving the second page of users statistic information
        val secondPageUsersRanking = repo.getUsersStats(
            offset = NonNegativeValue(limitValue).get(),
            limit = PositiveValue(limitValue).get()
        )

        // then:
        assertEquals(2, secondPageUsersRanking.currentPage)
        assertEquals(limitValue, secondPageUsersRanking.itemsPerPage)
    }

    @RepeatedTest(NR_OF_TEST_ITERATIONS)
    fun `can retrieve user stats by username query`() = runWithHandleAndRollback { handle ->
        // given: a UsersRepository
        val repo = JdbiUsersRepository(handle)

        // when: storing several users with similar usernames
        val nrOfUsers = 11 randomTo 30
        val queryFormat = "notARegularName"
        repeat(nrOfUsers) {
            val username = if (it % 2 == 0) {
                Username("$queryFormat-$it").get()
            } else {
                newTestUserName()
            }
            val email = newTestEmail()
            val passwordValidationInfo = PasswordValidationInfo(newTokenValidationData())
            repo.storeUser(username, email, passwordValidationInfo)
        }

        // and: retrieving the users by search term
        val searchTerm = Term(queryFormat).get()
        val userStatsByUsername = repo.getUserStatsByTerm(
            term = searchTerm,
            offset = NonNegativeValue(0).get(),
            limit = PositiveValue(nrOfUsers).get()
        )

        // and: calculating the expected number of users
        val actualNrOfUsers = if (nrOfUsers % 2 == 0) {
            nrOfUsers / 2
        } else {
            nrOfUsers / 2 + 1
        }

        // then: the users statistic information is paginated
        assertEquals(1, userStatsByUsername.currentPage)
        assertEquals(actualNrOfUsers, userStatsByUsername.itemsPerPage)
        assertEquals(actualNrOfUsers, userStatsByUsername.totalItems)

        // when: comparing with user information
        // then: the users statistic information is correct
        repeat(actualNrOfUsers) {
            val retrievedUserByUsername: User? = repo.getUserByUsername(userStatsByUsername.items[it].username)
            assertNotNull(retrievedUserByUsername)
            assertEquals(userStatsByUsername.items[it].username, retrievedUserByUsername.username)
            assertEquals(userStatsByUsername.items[it].email, retrievedUserByUsername.email)
        }
    }
}
