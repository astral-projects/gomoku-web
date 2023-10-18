package gomoku.repository.jdbi

import gomoku.TestClock
import gomoku.TestDataGenerator.newTestEmail
import gomoku.TestDataGenerator.newTestUserName
import gomoku.TestDataGenerator.newTokenValidationData
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

        // when: asking if the user exists
        val isUserIsStored = repo.isUserStoredByUsername(userName)

        // then: response is true
        assertTrue(isUserIsStored)

        // when: asking if a different user exists
        val anotherUserIsStored = repo.isUserStoredByUsername(newTestUserName())

        // then: response is false
        assertFalse(anotherUserIsStored)
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
        repo.createToken(token, 1)

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

        // when: TODO("continue updateTokenLastUsed test
    }

    @Test
    fun `can revoke tokens allowing a user to logout`() = runWithHandle { handle ->
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
        repo.createToken(token, 1)

        // when: retrieving the token and associated user
        val userAndToken = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)

        // then:
        val (_, retrievedToken) = userAndToken ?: fail("token and associated user must exist")

        // when: when token is revoked
        repo.revokeToken(retrievedToken.tokenValidationInfo)

        // then: the token is not able to be used again to keep login status active
        val userAndTokenAfterRevoke = repo.getTokenByTokenValidationInfo(testTokenValidationInfo)
        assertNull(userAndTokenAfterRevoke)
    }
}
