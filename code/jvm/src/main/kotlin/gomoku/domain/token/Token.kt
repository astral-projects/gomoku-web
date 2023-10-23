package gomoku.domain.token

import gomoku.domain.Id
import kotlinx.datetime.Instant

class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val userId: Id,
    val createdAt: Instant,
    val lastUsedAt: Instant
)
