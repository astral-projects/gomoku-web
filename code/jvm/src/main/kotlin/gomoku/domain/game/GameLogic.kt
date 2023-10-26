package gomoku.domain.game

import gomoku.domain.components.Id
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.isFinished
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.play
import gomoku.domain.game.errors.MakeMoveError
import gomoku.domain.game.variant.Variant
import gomoku.utils.Either
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

typealias GameMakeMoveResult = Either<MakeMoveError, Game>

/**
 * Defines the logic of the game.
 * @param variant - variant implementation to be used in the game.
 * @param clock - clock implementation.
 */
@Component
class GameLogic(
    private val variant: Variant,
    private val clock: Clock
) {

    /**
     * Player makes a move. The move is valid if the game is in progress and the position is empty.
     * @param square - Square position on the board.
     * @param game - game to which the user belongs.
     * @param userId - user who makes a move.
     * @return Game with new move.
     */
    fun play(square: Square, game: Game, userId: Id): GameMakeMoveResult {
        val board = game.board
        if (game.state != GameState.IN_PROGRESS) {
            return failure(MakeMoveError.GameOver)
        }
        if (board !is BoardRun) {
            return failure(MakeMoveError.GameOver)
        }
        if (board.turn?.player != userId.toPlayer(game) && board.turn != null) {
            return failure(MakeMoveError.NotYourTurn(board.turn.player))
        }
        return when (val newBoard = board.play(variant, square)) {
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

    /**
     * Function that returns a player based on the user id.
     * If the user is the host, the player is white, otherwise the player is black.
     *
     * @param game - game to which the user belongs
     */
    private fun Id.toPlayer(game: Game) = if (this == game.hostId) Player.W else Player.B
}
