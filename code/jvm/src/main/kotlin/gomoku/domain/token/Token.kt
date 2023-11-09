package gomoku.domain.token

import gomoku.domain.components.Id
import kotlinx.datetime.Instant

/**
 * Represents a token that can be used to authenticate a user.
 * @property tokenValidationInfo The information that can be used to validate the token.
 * @property userId The Id of the user that the token belongs to.
 * @property createdAt The [Instant] when the token was created.
 * @property lastUsedAt The [Instant] when the token was last used.
 */
class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Id,
    val createdAt: Instant,
    val lastUsedAt: Instant
)
