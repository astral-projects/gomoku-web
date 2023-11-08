package gomoku.http.pipeline

import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.services.game.GamesService
import org.springframework.stereotype.Component
import java.util.*

@Component
class IdempotencyKeyProcessor(
    val gameServices: GamesService
) {
    fun processIdempotencyKey(idempotencyKey: String?): IdempotencyKey? {
        if (idempotencyKey == null) {
            return null
        }

        return gameServices.getIdempotencyKeyInfo(UUID.fromString(idempotencyKey))?.let {
            IdempotencyKey(
                it.idempotencyKey,
                it.gameId,
                it.expirationDate
            )
        }
    }
}
