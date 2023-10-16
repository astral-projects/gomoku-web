package gomoku.services.user

import gomoku.domain.token.Token
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.UsersDomain
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.Either
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

    fun createUser(username: String, email: String, password: String): UserCreationResult {
        if (!usersDomain.isSafePassword(password)) {
            return failure(UserCreationError.InsecurePassword)
        }

        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)

        return transactionManager.run {
            val usersRepository = it.usersRepository
            if (usersRepository.isUserStoredByUsername(username)) {
                failure(UserCreationError.UserAlreadyExists)
            } else {
                val id = usersRepository.storeUser(username, email, passwordValidationInfo)
                success(id)
            }
        }
    }

    fun createToken(username: String, password: String): TokenCreationResult {
        if (username.isBlank() || password.isBlank()) {
            failure(TokenCreationError.UserOrPasswordAreInvalid)
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val user: User = usersRepository.getUserByUsername(username)
                ?: return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
            if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                if (!usersDomain.validatePassword(password, user.passwordValidation)) {
                    return@run failure(TokenCreationError.UserOrPasswordAreInvalid)
                }
            }
            val tokenValue = usersDomain.generateTokenValue()
            val now = clock.now()
            val newToken = Token(
                usersDomain.createTokenValidationInformation(tokenValue),
                user.id,
                createdAt = now,
                lastUsedAt = now
            )
            usersRepository.createToken(newToken, usersDomain.maxNumberOfTokensPerUser)
            Either.Right(
                TokenExternalInfo(
                    tokenValue,
                    usersDomain.getTokenExpiration(newToken)
                )
            )
        }
    }

    fun getUserByToken(token: String): User? {
        if (!usersDomain.canBeToken(token)) {
            return null
        }
        return transactionManager.run {
            val usersRepository = it.usersRepository
            val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
            val userAndToken = usersRepository.getTokenByTokenValidationInfo(tokenValidationInfo)
            if (userAndToken != null ){//TODO(IT WAS GIVING ME PROBLEMS SO I COMMENTED THIS)&& usersDomain.isTokenTimeValid(clock, userAndToken.second)) {
                usersRepository.updateTokenLastUsed(userAndToken.second, clock.now())
                userAndToken.first
            } else {
                null
            }
        }
    }

    fun revokeToken(token: String): Boolean {
        val tokenValidationInfo = usersDomain.createTokenValidationInformation(token)
        return transactionManager.run {
            it.usersRepository.logout(tokenValidationInfo)
            true
        }
    }

    fun getUsersRanking(): List<UserRankInfo> {
        TODO("Not yet implemented")
    }

    fun editUser(user: User): User {
        TODO("Not yet implemented")
    }
}
