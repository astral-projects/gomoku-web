package gomoku.repository

import gomoku.domain.PaginatedResult
import gomoku.domain.UserAndToken
import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PositiveValue
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.UserStatsInfo
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username
import kotlinx.datetime.Instant

/**
 * Repository for managing user-related data.
 */
interface UsersRepository {

    /**
     * Stores a user in the database.
     * @param username the username of the user to store.
     * @param email the email of the user to store.
     * @param passwordValidation the object that can validate this user's password in the future.
     * @return the id of the created user.
     */
    fun storeUser(username: Username, email: Email, passwordValidation: PasswordValidationInfo): Id

    /**
     * Retrieves a user by their username.
     * @param username the username of the user to retrieve.
     * @return the user or null if no such user exists.
     */
    fun getUserByUsername(username: Username): User?

    /**
     * Retrieves a user by username and associated token.
     * @param tokenValidationInfo the object that can validate a token.
     * @return the user and token or null if no such user exists.
     */
    fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): UserAndToken?

    /**
     * Checks if a user with the given email exists.
     * @param email the email of the user to retrieve.
     * @return true if such a user exists, false otherwise.
     */
    fun isUserStoredByEmail(email: Email): Boolean

    /**
     * Creates a token for the given user.
     * @param token the token to create.
     * @param maxTokens the maximum number of tokens the user can have available.
     */
    fun createToken(token: Token, maxTokens: PositiveValue)

    /**
     * Updates the last used time of the given token.
     * @param token the token to retrieve.
     * @param now the time to set as the last used time.
     */
    fun updateTokenLastUsed(token: Token, now: Instant)

    /**
     * Retrieves a user by their id.
     * @param userId the id of the user to retrieve.
     * @return the user or null if no such user exists.
     */
    fun getUserById(userId: Id): User?

    /**
     * Revokes a token, making it invalid.
     * @param tokenValidationInfo the object that can validate a token.
     * @return true if the token was revoked successfully, false otherwise.
     */
    fun revokeToken(tokenValidationInfo: TokenValidationInfo): Boolean

    /**
     * Retrieves user's statistics information.
     * @param offset the offset to start the result from.
     * @param limit the maximum number of results to return.
     * @return the user's statistic information in a paginated result.
     * The results are ordered by the user's rank.
     */
    fun getUsersStats(offset: NonNegativeValue, limit: PositiveValue): PaginatedResult<UserStatsInfo>

    /**
     * Retrieves single user statistic information.
     * @param userId the id of the user to retrieve.
     * @return the user statistic information or null if no such user exists.
     */
    fun getUserStats(userId: Id): UserStatsInfo?

    /**
     * Retrieves users' statistics information by username query.
     * @param username the username to search for.
     * @param limit the maximum number of results to return.
     * @param offset the offset to start the result from.
     * @return the user's statistic information in a paginated result.
     */
    fun getUserStatsByUsername(
        username: Username,
        limit: PositiveValue,
        offset: NonNegativeValue,
    ): PaginatedResult<UserStatsInfo>
}
