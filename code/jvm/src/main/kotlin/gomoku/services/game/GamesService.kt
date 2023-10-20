package gomoku.services.game

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.GameLogic
import gomoku.domain.game.GamePoints
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.*
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.repository.GamesRepository
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.failure
import gomoku.utils.success
import org.springframework.stereotype.Component

@Component
class GamesService(
    private val transactionManager: TransactionManager,
    private val gameLogic: GameLogic
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
            val lobby = gamesRepository.isMatchmaking(variantId, user.id)
            val variant = gamesRepository.getVariantById(variantId)
            if (variant == null) {
                failure(GameCreationError.VariantNotFound)
            }
            if (lobby != null) {
                gamesRepository.deleteUserFromLobby(user.id)
                val res = gamesRepository.createGame(
                    variantId = variantId,
                    hostId = lobby.userId,
                    guestId = user.id,
                    lobbyId = lobby.lobbyId
                )
                when (res) {
                    false -> failure(GameCreationError.UserAlreadyInGame)
                    true -> success("Joining game")
                }
            } else {
                val check = gamesRepository.checkIfIsLobby(user.id)
                if (check) {
                    failure(GameCreationError.UserAlreadyInLobby)
                } else {
                    val r = gamesRepository.waitInLobby(variantId, user.id)
                    // TODO(I think we need to create a argument resolver for the VariandInputModel,
                    // beacuse if you pass an Integer that isnt created in the database, it will throw an exception)
                    when (r) {
                        false -> failure(GameCreationError.VariantNotFound)
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

    fun getGameStatus(userId: Id, gameId: Id): GettingGameResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            when (val game = gamesRepository.getGameStatus(gameId, userId)) {
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

    fun makeMove(gameId: Id, userId: Id, square: Square, player: Player): GameMakeMoveResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val game = gamesRepository.getGameById(gameId)
                ?: return@run failure(GameMakeMoveError.GameNotFound)
            if (!gamesRepository.userBelongsToTheGame(userId, gameId)) {
                failure(GameMakeMoveError.UserDoesNotBelongToThisGame)
            }
            val playLogic = gameLogic.play(square, game, userId)
            if (playLogic is Failure) {
                failure(GameMakeMoveError.MoveNotValid(playLogic.value))
            } else if (playLogic is Success) {
                gamePointsBoard(gamesRepository, gameId, userId, game, playLogic.value.board)
                when (val makeMove = gamesRepository.updateGame(gameId, playLogic.value.board)) {
                    false -> failure(GameMakeMoveError.GameNotFound)
                    true -> success(makeMove)
                }
            }
            success(true)
        }
    }

    fun exitGame(gameId: Id, userId: Id): GameDeleteResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val winner = gamesRepository.exitGame(gameId, userId)
            if (winner != null) {
                val p = GamePoints()
                gamesRepository.updatePoints(
                    gameId,
                    winner,
                    userId,
                    p.winner_points,
                    p.loser_points,
                    p.itsDraw
                )
                success(true)
            }else{
                failure(GameDeleteError.GameNotFound)
            }
        }
    }

    private fun gamePointsBoard(gamesRepository:GamesRepository, gameId: Id, userId: Id, game: Game, board:Board):Boolean =
        when (board) {
            is BoardWin -> {
                val p = GamePoints()
                gamesRepository.updatePoints(
                    gameId,
                    userId,
                    if (userId == game.hostId) game.guestId else game.hostId,
                    p.winner_points,
                    p.loser_points,
                    p.itsWin
                )
            }

            is BoardDraw -> {
                val p = GamePoints()
                gamesRepository.updatePoints(
                    gameId,
                    userId,
                    if (userId == game.hostId) game.guestId else game.hostId,
                    p.draw_points,
                    p.draw_points,
                    p.itsDraw
                )
            }

            is BoardRun -> {
                true
            }
        }

}


