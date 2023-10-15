package gomoku.domain.token

import gomoku.domain.user.UserId
import kotlinx.datetime.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: UserId,
    val createdAt: Instant,
    val lastUsedAt: Instant
)
