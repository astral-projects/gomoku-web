package gomoku.repository.jdbi

import gomoku.utils.TestClock
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.TestDataGenerator.newTokenValidationData
import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.repository.jdbi.JdbiTestConfiguration.runWithHandle
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

// Don't forget to ensure DBMS is up (e.g. by running ./gradlew dbTestsWait)
class JdbiUserRepositoryTests {

    @Test
    fun `can create and retrieve user`() = runWithHandle { handle ->
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

        // when: asking if the user exists by username
        val isUserIsStored = repo.isUserStoredByUsername(userName)

        // then: response is true
        assertTrue(isUserIsStored)

        // when: asking if the user exists by email
        val isUserIsStoredByEmail = repo.isUserStoredByEmail(email)

        // then: response is true
        assertTrue(isUserIsStoredByEmail)

        // when: asking if a user exists by username that does not exist
        val anotherUserIsStoredByUsername = repo.isUserStoredByUsername(newTestUserName())

        // then: response is false
        assertFalse(anotherUserIsStoredByUsername)

        // when: asking if a user exists by email that does not exist
        val anotherUserIsStoredByEmail = repo.isUserStoredByEmail(newTestEmail())

        // then: response is false
        assertFalse(anotherUserIsStoredByEmail)
    }

    @Test
    fun `can create, validate and update tokens`() = runWithHandle { handle ->
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
        repo.createToken(token, PositiveValue(1))

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

    @Test
    fun `can revoke tokens`() = runWithHandle { handle ->
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
        repo.createToken(token, PositiveValue(1))

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

    @Test
    fun `retrieves all users ranking information that is paginated`() = runWithHandle { handle ->
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

        // and: retrieving the first 3 users ranking information
        val limitValue = 3
        val usersRanking = repo.getUsersRanking(
            offset = NonNegativeValue(0),
            limit = PositiveValue(limitValue)
        )

        // then: the users ranking is paginated
        assertEquals(1, usersRanking.currentPage)
        assertEquals(limitValue, usersRanking.itemsPerPage)

        // when: comparing with user information
        // then: the users ranking information is correct
        repeat(3) {
            val retrievedUserByUsername: User? = repo.getUserByUsername(usersRanking.items[it].username)
            assertNotNull(retrievedUserByUsername)
            assertEquals(usersRanking.items[it].username, retrievedUserByUsername.username)
            assertEquals(usersRanking.items[it].email, retrievedUserByUsername.email)
        }

        // when: retrieving all users ranking information
        val secondLimitValue = nrOfUsers
        val allUsersRanking = repo.getUsersRanking(
            offset = NonNegativeValue(0),
            limit = PositiveValue(secondLimitValue)
        )

        // then: the users ranking is paginated
        assertEquals(1, allUsersRanking.currentPage)
        assertEquals(secondLimitValue, allUsersRanking.itemsPerPage)

        // when: when retrieving the second page of users ranking information
        val secondPageUsersRanking = repo.getUsersRanking(
            offset = NonNegativeValue(limitValue),
            limit = PositiveValue(limitValue)
        )

        // then:
        assertEquals(2, secondPageUsersRanking.currentPage)
        assertEquals(limitValue, secondPageUsersRanking.itemsPerPage)
    }
}
