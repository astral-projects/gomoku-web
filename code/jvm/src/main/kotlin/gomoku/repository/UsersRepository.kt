package gomoku.repository

import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.UserRankingInfo
import kotlinx.datetime.Instant

interface UsersRepository {

    fun storeUser(username: String, email: String, passwordValidation: PasswordValidationInfo): Int
    fun getUserByUsername(username: String): User?
    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>?
    fun isUserStoredByUsername(username: String): Boolean
    fun createToken(token: Token, maxTokens: Int)
    fun updateTokenLastUsed(token: Token, now: Instant)
    fun logout(tokenValidationInfo: TokenValidationInfo): Int
    fun getUsersRanking(): List<UserRankingInfo>
    fun getUserStats(userId: Int): UserRankingInfo?
    fun editUser(user: User): Boolean
}
