package gomoku.domain.idempotencyKey

import gomoku.domain.components.Id
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*

class IdempotencyKey(
    val idempotencyKey: UUID,
    val gameId: Id,
    val expirationDate: Instant
) {
    fun isExpired(clock: Clock): Boolean {
        val now = clock.now()
        return expirationDate < now
    }
}
