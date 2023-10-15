package gomoku.services

import gomoku.domain.game.Game
import gomoku.repository.transaction.TransactionManager
import org.springframework.stereotype.Component

@Component
class GamesService(
    private val transactionManager: TransactionManager,
) {

    fun getGameById(id: Int): Game? =
        transactionManager.run {
            it.gamesRepository.getGameById(id)
        }

    fun createGame(gameVariant:String,openingRule: String,boardSize: Int, host:Int, guest:Int): Int? =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.createGame(gameVariant,openingRule,boardSize,host,guest)
        }

    fun deleteGame(game: Game) {
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.deleteGame(game)
        }
    }
}