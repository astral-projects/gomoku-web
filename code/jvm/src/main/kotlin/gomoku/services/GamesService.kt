package gomoku.services

import gomoku.domain.game.Game
import gomoku.repository.TransactionManager
import org.springframework.stereotype.Component

@Component
class GamesService(
    private val transactionManager: TransactionManager,
) {

    fun getGameById(id: Int): Game? =
        transactionManager.run { trans ->
            val gamesRepository = trans.gamesRepository
            gamesRepository.getGameById(id)
        }

}
