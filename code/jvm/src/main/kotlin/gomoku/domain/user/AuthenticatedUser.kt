package gomoku.domain.user

/**
 * Represents a user and its retrieved raw token.
 * @property user The user.
 * @property token The raw token.
 */
data class AuthenticatedUser(
    val user: User,
    val token: String
)
