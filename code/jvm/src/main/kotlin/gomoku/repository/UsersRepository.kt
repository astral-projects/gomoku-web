package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PaginatedResult
import gomoku.domain.PositiveValue
import gomoku.domain.UserAndToken
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.Email
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.Username
import gomoku.utils.NotTested
import kotlinx.datetime.Instant

interface UsersRepository {
    fun storeUser(username: Username, email: Email, passwordValidation: PasswordValidationInfo): Id
    fun getUserByUsername(username: Username): User?
    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): UserAndToken?
    fun isUserStoredByUsername(username: Username): Boolean
    fun isUserStoredByEmail(email: Email): Boolean
    fun createToken(token: Token, maxTokens: PositiveValue)
    fun updateTokenLastUsed(token: Token, now: Instant)
    fun getUserById(userId: Id): User?
    fun revokeToken(tokenValidationInfo: TokenValidationInfo): Boolean
    fun getUsersRanking(offset: NonNegativeValue, limit: PositiveValue): PaginatedResult<UserRankInfo>

    @NotTested
    fun getUserStats(userId: Id): UserRankInfo?

    @NotTested
    fun editUser(userId: Id): User
}
