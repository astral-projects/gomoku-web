package gomoku.services

import gomoku.domain.game.Game
import gomoku.domain.game.OpeningRule
import gomoku.domain.game.board.BoardSize
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