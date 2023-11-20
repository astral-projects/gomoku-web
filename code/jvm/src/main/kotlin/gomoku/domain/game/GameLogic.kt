package gomoku.domain.game

import gomoku.domain.components.Id
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.isFinished
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.play
import gomoku.domain.game.variant.Variant
import gomoku.services.game.MakeMoveResult
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock

/**
 * Represents the game logic, encapsulating the rules of the game given by the variant.
 * @property variant The variant of the game.
 * @property clock The clock used to get the current time.
 */
class GameLogic(
    private val variant: Variant,
    private val clock: Clock
) {

    /**
     * Makes a move on the board.
     * @param game game to which the user belongs.
     * @param userId user who makes a move.
     * @param toSquare square where the move is being made.
     * @return the updated game if the move is valid, or an error otherwise.
     */
    fun play(game: Game, userId: Id, toSquare: Square): MakeMoveResult {
        val board = game.board
        if (board !is BoardRun || game.state === GameState.FINISHED) {
            return failure(MakeMoveError.GameOver)
        }
        val localPlayer = userId.toPlayer(game)
        if (board.turn?.player != localPlayer && board.turn != null) {
            return failure(MakeMoveError.NotYourTurn(board.turn.player))
        }
        return when (val newBoard = board.play(variant, localPlayer, toSquare)) {
            is Failure -> when (newBoard.value) {
                MakeMoveError.GameOver -> Failure(MakeMoveError.GameOver)
                is MakeMoveError.NotYourTurn -> Failure(MakeMoveError.NotYourTurn(newBoard.value.player))
                is MakeMoveError.PositionTaken -> Failure(MakeMoveError.PositionTaken(newBoard.value.square))
                is MakeMoveError.InvalidPosition -> Failure(MakeMoveError.InvalidPosition(newBoard.value.square))
            }

            is Success -> success(
                game.copy(
                    board = newBoard.value,
                    state = if (newBoard.value.isFinished()) GameState.FINISHED else GameState.IN_PROGRESS,
                    updatedAt = clock.now()
                )
            )
        }
    }

    companion object {
        /**
         * Converts a user id to a player on the board.
         * White player is always the host, black player is always the guest.
         * @param game game to which the user belongs.
         */
        fun Id.toPlayer(game: Game) = if (this == game.hostId) Player.W else Player.B
    }
}