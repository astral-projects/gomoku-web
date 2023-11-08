package gomoku.http.pipeline

import gomoku.domain.components.Id
import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.services.game.GamesService
import gomoku.utils.get
import org.springframework.stereotype.Component
import java.util.*

@Component
class IdempotencyKeyProcessor(
    val gameServices: GamesService
) {
    fun processIdempotencyKey(idempotencyKey: String?, gameId: Int?): IdempotencyKey? {
        if (idempotencyKey == null || gameId == null) {
            return null
        }
        return gameServices.getIdempotencyKeyInfo(UUID.fromString(idempotencyKey), Id(gameId).get())?.let {
            IdempotencyKey(
                it.idempotencyKey,
                it.gameId,
                it.expirationDate
            )
        }
    }
}
