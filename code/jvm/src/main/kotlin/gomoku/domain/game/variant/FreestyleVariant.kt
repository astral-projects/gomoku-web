package gomoku.domain.game.variant

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.GamePointsOnDraw
import gomoku.domain.game.GamePointsOnForfeitOrTimer
import gomoku.domain.game.GamePointsOnWin
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.errors.BoardMakeMoveResult
import gomoku.domain.game.errors.MakeMoveError
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.domain.game.variant.config.VariantName
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.get
import org.springframework.stereotype.Component

// Constants
private const val WINNING_PIECES = 5
private const val WINNER_POINTS = 300
private const val LOSER_POINTS = 100
private const val DRAW_POINTS = 150
private const val WINNER_ON_FORFEIT_OR_TIMER_POINTS = 400
private const val FORFEITER_POINTS = 0
private const val MAX_TURN_TIME_IN_SEC = 60

/**
 * Represents the FreeStyle variant of the game.
 * Freestyle gomoku has no restrictions on either player and allows a player to win by creating a line
 * of five or more stones, with each player alternating turns placing one stone at a time.
 * @see <a href="https://en.wikipedia.org/wiki/Gomoku#Freestyle">Freestyle</a>
 */
@Component
class FreestyleVariant : Variant {
    override val config: VariantConfig = VariantConfig(VariantName.FREESTYLE, OpeningRule.NONE, BoardSize.FIFTEEN)

    override fun isMoveValid(board: Board, square: Square): BoardMakeMoveResult {
        when (board) {
            is BoardWin, is BoardDraw -> return Failure(MakeMoveError.GameOver)
            is BoardRun -> {
                if (square.row.toIndex() >= config.boardSize.size || square.col.toIndex() >= config.boardSize.size) {
                    return Failure(MakeMoveError.InvalidPosition(square))
                }
                if (square in board.grid) return Failure(MakeMoveError.PositionTaken(square))
                if (board.turn == null) return Failure(MakeMoveError.NotYourTurn(Player.B))
                val updatedMoves = board.grid + Move(square, Piece(board.turn.player))
                return when {
                    checkWin(board, square) -> Success(BoardWin(updatedMoves, board.turn.player))
                    checkDraw(updatedMoves) -> Success(BoardDraw(updatedMoves))
                    else -> Success(BoardRun(updatedMoves, board.turn.other()))
                }
            }
        }
    }

    override fun initialBoard(): Board = BoardRun(emptyMap(), BoardTurn(Player.W, turnTimer))

    override val points: GamePoints
        get() = GamePoints(
            onFinish = GamePointsOnWin(
                winner = NonNegativeValue(WINNER_POINTS).get(),
                loser = NonNegativeValue(LOSER_POINTS).get()
            ),
            onDraw = GamePointsOnDraw(
                shared = NonNegativeValue(DRAW_POINTS).get()
            ),
            onForfeitOrTimer = GamePointsOnForfeitOrTimer(
                winner = NonNegativeValue(WINNER_ON_FORFEIT_OR_TIMER_POINTS).get(),
                forfeiter = NonNegativeValue(FORFEITER_POINTS).get()
            )
        )

    override val turnTimer: NonNegativeValue
        get() = NonNegativeValue(MAX_TURN_TIME_IN_SEC).get()

    /**
     * Checks if the game is a draw, according to this variant rules.
     * @param updatedMoves The updated moves of the game.
     * @return true if the game is a draw, false otherwise.
     */
    private fun checkDraw(updatedMoves: Moves): Boolean =
        updatedMoves.size == config.boardSize.size * config.boardSize.size

    /**
     * Checks if the game is a win, according to this variant rules.
     * @param board The game board.
     * @param square The square where the move is being made to.
     * @return true if the game is a win, false otherwise.
     */
    private fun checkWin(board: Board, square: Square): Boolean {
        return try {
            requireNotNull(board.turn)
            if (board.turn.timeLeftInSec.value <= 0) {
                return true
            }
            val backSlash = square.row.toIndex() == square.col.toIndex()
            val slash = square.row.toIndex() + square.col.toIndex() == config.boardSize.size - 1
            val places = board.grid.filter { it.value == Piece(board.turn.player) }.keys + square
            places.count { it.col == square.col } >= WINNING_PIECES ||
                    places.count { it.row == square.row } >= WINNING_PIECES ||
                    places.count { backSlash } >= WINNING_PIECES ||
                    places.count { slash } >= WINNING_PIECES
        } catch (ex: IllegalArgumentException) {
            // only way found to use smart cast
            false
        }
    }
}
