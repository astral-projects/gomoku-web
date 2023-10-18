package gomoku.services.game

import gomoku.domain.Id
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.domain.user.UsersDomain
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.failure
import gomoku.utils.success
import org.springframework.stereotype.Component

@Component
class GamesService(
    private val transactionManager: TransactionManager,
    private val usersDomain: UsersDomain,
) {

    fun getGameById(id: Id): GettingGameResult {
        return transactionManager.run {
            val game = (it.gamesRepository.getGameById(id))
            when (game) {
                null -> failure(GettingGameError.GameNotFound)
                else -> success(game)
            }
        }
    }

    fun findGame(variantId: Id, user: User): GameCreationResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val lobby = gamesRepository.isMatchmaking(variantId)
            if (lobby != null) {
                gamesRepository.deleteUserFromLobby(user.id)
                val res = gamesRepository.createGame(
                    variantId = variantId,
                    hostId = lobby.userId,
                    guestId = user.id,
                    lobbyId = lobby.lobbyId
                )
                val res = gamesRepository.createGame(variantId, lobby.userId, user.id, lobby.lobbyId)
                when (res) {
                    false -> failure(GameCreationError.UserAlreadyInGame)
                    true -> success("Joining game")
                }
            } else {
                val check = gamesRepository.checkIfIsLobby(user.id)
                if(check) {
                    failure(GameCreationError.UserAlreadyInLobby)
                }else {
                    val r = gamesRepository.insertInLobby(variantId, user.id)
                    //TODO(I think we need to create a argument resolver for the VariandInputModel,
                    // beacuse if you pass an Integer that isnt created in the database, it will throw an exception)
                    when (r) {
                        false -> failure(GameCreationError.GameVariantNotFound)
                        true -> success("Waiting in lobby")
                    }
                }
            }
        }
    }

    fun deleteGame(gameId: Id, userId: Id): GamePutResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val u = gamesRepository.userIsTheHost(gameId, userId)
            if (!u) {
                failure(GamePutError.UserIsNotTheHost)
            } else {
                val g = gamesRepository.deleteGame(gameId, userId)
                when (g) {
                    false -> failure(GamePutError.GameNotFound)
                    true -> success(g)
                }
            }
        }
    }

    fun getGameStatus(user: User, gameId: Id): GettingGameResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            when (val game = gamesRepository.getGameStatus(gameId, user)) {
                null -> failure(GettingGameError.GameNotFound)
                else -> success(game)
            }
        }
    }

    fun getSystemInfo(): SystemInfo =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.getSystemInfo()
        }

    fun makeMove(gameId: Id, user: User, square: Square, player: Player): GameMakeMoveResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            if (!gamesRepository.userBelongsToTheGame(user, gameId)) {
                failure(GameMakeMoveError.UserDoesNotBelongToThisGame)
            }
            if (!gamesRepository.makeMove(gameId, user.id, square, player)) {
                failure(GameMakeMoveError.MoveNotValid)
            }
            success(true)
        }
    }

    fun exitGame(gameId: Id, user: User): GameDeleteResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val res = gamesRepository.exitGame(gameId, user)
            if (res){
                gamesRepository.updatePoints(gameId,user.id)

            }
            when (res) {
                false -> failure(GameDeleteError.GameNotFound)
                true -> success(res)
            }
        }
    }
}
