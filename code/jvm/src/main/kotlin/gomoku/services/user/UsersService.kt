package gomoku.services.user

import gomoku.domain.PaginatedResult
import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PositiveValue
import gomoku.domain.token.Token
import gomoku.domain.user.User
import gomoku.domain.user.UserStatsInfo
import gomoku.domain.user.UsersDomain
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Password
import gomoku.domain.user.components.Username
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.NotTested
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
class UsersService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock
) {

    fun createUser(username: Username, email: Email, password: Password): UserCreationResult {
        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password.value)
        return transactionManager.run { transaction ->
            val usersRepository = transaction.usersRepository
            if (usersRepository.getUserByUsername(username) != null) {
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
                ?: return@run failure(TokenCreationError.UsernameNotExists)
            if (!usersDomain.validatePassword(password.value, user.passwordValidation)) {
                return@run failure(TokenCreationError.PasswordIsWrong)
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

    fun getUsersStats(offset: NonNegativeValue, limit: PositiveValue): PaginatedResult<UserStatsInfo> =
        transactionManager.run {
            it.usersRepository.getUsersStats(offset, limit)
        }

    @NotTested
    fun getUserStats(userId: Id): UserStatsInfo? =
        transactionManager.run {
            it.usersRepository.getUserStats(userId)
        }

    @NotTested
    fun editUser(user: User): User {
        TODO("Not yet implemented")
    }
}
