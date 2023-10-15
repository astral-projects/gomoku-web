package gomoku.services

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.domain.user.UserId
import gomoku.domain.user.UsersDomain
import gomoku.repository.transaction.TransactionManager
import org.springframework.stereotype.Component

@Component
class GamesService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
) {

    fun getGameById(id: Int): Game? =
        transactionManager.run {
            it.gamesRepository.getGameById(id)
        }

    fun startGame(gameVariant:String, openingRule: String, boardSize: Int,user:User): Int? =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.startGame(gameVariant,openingRule,boardSize,user.id.value)
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