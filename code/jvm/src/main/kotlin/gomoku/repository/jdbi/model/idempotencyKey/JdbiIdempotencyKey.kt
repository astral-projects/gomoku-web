package gomoku.repository.jdbi.model.idempotencyKey

import gomoku.domain.components.Id
import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.repository.jdbi.model.JdbiModel
import gomoku.utils.get
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.util.*

class JdbiIdempotencyKey(
    @ColumnName("idempotency_key")
    val idempotencyKey: String,
    @ColumnName("game_id")
    val gameId: Int,
    @ColumnName("expires_at")
    val expiresAt: Instant
) : JdbiModel<IdempotencyKey> {
    override fun toDomainModel(): IdempotencyKey {
        return IdempotencyKey(
            idempotencyKey = UUID.fromString(idempotencyKey),
            gameId = Id(gameId).get(),
            expirationDate = expiresAt
        )
    }
}
