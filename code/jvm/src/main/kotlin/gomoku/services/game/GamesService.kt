package gomoku.services.game

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.GameId
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.domain.user.UsersDomain
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.Response
import org.springframework.stereotype.Component

@Component
class GamesService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
) {

    fun getGameById(id: Id): Game? =
        transactionManager.run {
            it.gamesRepository.getGameById(id)
        }

    fun startGame(variantId: Id, user: User): Boolean =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.startGame(variantId, user.id)
        }

    fun deleteGame(game: Game) {
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.deleteGame(game)
        }
    }

    fun getGameStatus(user: User, gameId: Int): String? =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.getGameStatus(gameId, user)
        }

    fun getSystemInfo(): SystemInfo =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.getSystemInfo()
        }

    fun makeMove(gameId: Id, userId: Id, square: Square): Boolean {
        TODO("Not yet implemented")
    }

    fun makeMove(gameId: GameId, user: User, square: Square, player: Player):Response =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            if(!gamesRepository.userBelongsToTheGame(user, gameId)){
                return@run Response(403, reasonException = "This user don´t have permissions to this game")
            }
            if(!gamesRepository.makeMove(gameId, user, square, player)){
                return@run Response(404 , reasonException = "Move not valid do this game")
            }
            return@run Response(200, reasonException = "Your move is added do the board")
        }


    fun exitGame(gameId: Id, user: User): Boolean {
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.exitGame(gameId, user)
        }
        return true
    }
}
