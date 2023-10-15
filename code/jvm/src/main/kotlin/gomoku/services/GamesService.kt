package gomoku.services

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.UserId
import gomoku.repository.transaction.TransactionManager
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

    fun getSystemInfo() {
        TODO("Not yet implemented")

    }

    fun makeMove(gameId: GameId, userId: UserId, square: Square): Boolean {
        TODO("Not yet implemented")
    }

    fun exitGame(gameId: GameId): Boolean {
        TODO("Not yet implemented")
    }
}