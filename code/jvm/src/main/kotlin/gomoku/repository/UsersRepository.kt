package gomoku.repository

import kotlinx.datetime.Instant
import gomoku.domain.PasswordValidationInfo
import gomoku.domain.Token
import gomoku.domain.TokenValidationInfo
import gomoku.domain.User

interface UsersRepository {

    fun storeUser(
        username: String,
        passwordValidation: PasswordValidationInfo
    ): Int

    fun getUserByUsername(username: String): User?

    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?

    fun isUserStoredByUsername(username: String): Boolean

    fun createToken(token: Token, maxTokens: Int)

    fun updateTokenLastUsed(token: Token, now: Instant)

    fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int
}
