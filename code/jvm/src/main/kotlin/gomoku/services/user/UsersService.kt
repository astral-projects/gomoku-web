package gomoku.services.user

import gomoku.domain.Id
import gomoku.domain.token.Token
import gomoku.domain.user.Email
import gomoku.domain.user.Password
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.Username
import gomoku.domain.user.UsersDomain
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.NotTested
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

@Component
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock
) {

    fun createUser(username: Username, email: Email, password: Password): UserCreationResult {
        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password.value)
        return transactionManager.run { transaction ->
            val usersRepository = transaction.usersRepository
            if (usersRepository.isUserStoredByUsername(username)) {
                failure(UserCreationError.UsernameAlreadyExists)
            } else if (usersRepository.isUserStoredByEmail(email)) {
                failure(UserCreationError.EmailAlreadyExists)
            } else {
                success(usersRepository.storeUser(username, email, passwordValidationInfo))
            }
        }
    }

    fun getUserById(userId: Id): GettingUserResult =
        transactionManager.run { transaction ->
            val usersRepository = transaction.usersRepository
            val user: User = usersRepository.getUserById(userId)
                ?: return@run failure(GettingUserError.UserNotFound)
            success(user)
        }

    fun createToken(username: Username, password: Password): TokenCreationResult =
        transactionManager.run {
            val usersRepository = it.usersRepository
            val user: User = usersRepository.getUserByUsername(username)
                ?: return@run failure(TokenCreationError.UsernameIsInvalid)
            if (!usersDomain.validatePassword(password.value, user.passwordValidation)) {
                return@run failure(TokenCreationError.PasswordIsInvalid)
            }
            val tokenValue = usersDomain.generateTokenValue()
            val newToken = Token(
                tokenValidationInfo = usersDomain.createTokenValidationInformation(tokenValue),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now()
            )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            success(
                TokenExternalInfo(
                    tokenValue,
                    usersDomain.getTokenExpiration(newToken)
                )
            )
        }

    fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null && usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun revokeToken(token: String): TokenRevocationResult {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (usersRepository.revokeToken(tokenValidationInfo)) {
                success(true)
            } else {
                failure(TokenRevocationError.TokenIsInvalid)
            }
        }
    }

    @NotTested
    fun getUsersRanking(): List<UserRankInfo> {
        TODO("Not yet implemented")
    }

    @NotTested
    fun getUserStats(userId: Id): UserRankInfo? {
        TODO("Not yet implemented")
    }

    @NotTested
    fun editUser(user: User): User {
        TODO("Not yet implemented")
    }
}
