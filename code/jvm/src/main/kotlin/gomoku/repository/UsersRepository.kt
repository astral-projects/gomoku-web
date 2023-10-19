package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.UserAndToken
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.Email
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.Username
import kotlinx.datetime.Instant

interface UsersRepository {
    fun storeUser(username: Username, email: Email, passwordValidation: PasswordValidationInfo): Id // tested
    fun getUserByUsername(username: Username): User? // tested
    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): UserAndToken? // tested
    fun isUserStoredByUsername(username: Username): Boolean // tested
    fun isUserStoredByEmail(email: Email): Boolean
    fun createToken(token: Token, maxTokens: Int) // tested
    fun updateTokenLastUsed(token: Token, now: Instant)
    fun getUserById(userId: Id): User? // tested
    fun revokeToken(tokenValidationInfo: TokenValidationInfo): Boolean // tested
    fun getUsersRanking(): List<UserRankInfo>
    fun getUserStats(userId: Id): UserRankInfo?
    fun editUser(userId: Id): User
}
