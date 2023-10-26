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

private const val WINNING_PIECES = 5

// TODO("revisited game variant logic")
@Component
class FreestyleVariant : Variant {
    override val config: VariantConfig = VariantConfig(VariantName.FREESTYLE, OpeningRule.PRO, BoardSize.FIFTEEN)

    override fun isMoveValid(board: Board, square: Square): BoardMakeMoveResult {
        when (board) {
            is BoardWin, is BoardDraw -> return Failure(MakeMoveError.GameOver)
            is BoardRun -> {
                if (square.row.toIndex() >= config.boardSize.size || square.col.toIndex() >= config.boardSize.size) {
                    return Failure(MakeMoveError.InvalidPosition(square))
                }
                if (square in board.grid) return Failure(MakeMoveError.PositionTaken(square))
                if (board.turn == null) return Failure(MakeMoveError.NotYourTurn(Player.B))
                val moves = board.grid + Move(square, Piece(board.turn.player))
                return when {
                    checkWin(board, square) -> Success(BoardWin(moves, board.turn.player))
                    moves.size == config.boardSize.size * config.boardSize.size -> Success(BoardDraw(moves))
                    else -> Success(BoardRun(moves, board.turn.other()))
                }
            }
        }
    }

    override fun checkWin(board: Board, square: Square): Boolean {
        return try {
            requireNotNull(board.turn)
            val backSlash = square.row.toIndex() == square.col.toIndex()
            val slash = square.row.toIndex() + square.col.toIndex() == config.boardSize.size - 1
            val places = board.grid.filter { it.value == Piece(board.turn.player) }.keys + square
            places.count { it.col == square.col } == WINNING_PIECES ||
                    places.count { it.row == square.row } == WINNING_PIECES ||
                    places.count { backSlash } == WINNING_PIECES ||
                    places.count { slash } == WINNING_PIECES ||
                    board.turn.timeLeftInSec.value <= 0
        } catch (ex: IllegalArgumentException) {
            // only way found to use smart cast
            false
        }
    }

    override fun initialBoard(): Board = BoardRun(emptyMap(), BoardTurn(Player.W, turnTimer))

    override val points: GamePoints
        get() = GamePoints(
            onFinish = GamePointsOnWin(winner = NonNegativeValue(300).get(), loser = NonNegativeValue(100).get()),
            onDraw = GamePointsOnDraw(shared = NonNegativeValue(150).get()),
            onForfeitOrTimer = GamePointsOnForfeitOrTimer(
                winner = NonNegativeValue(400).get(),
                forfeiter = NonNegativeValue(0).get()
            )
        )

    override val turnTimer: NonNegativeValue
        get() = NonNegativeValue(60).get()
}
