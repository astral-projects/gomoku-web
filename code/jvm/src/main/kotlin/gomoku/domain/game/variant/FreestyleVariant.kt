package gomoku.domain.game.variant

import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.GamePointsOnDraw
import gomoku.domain.game.GamePointsOnForfeitOrTimer
import gomoku.domain.game.GamePointsOnWin
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import org.springframework.stereotype.Component

private const val WINNING_PIECES = 5

@Component
// TODO("revisited game logic")
class FreestyleVariant : Variant {
    override val config: VariantConfig = VariantConfig(VariantName.FREESTYLE, OpeningRule.PRO, BoardSize.FIFTEEN)

    // TODO("change return type to Either<MakeMoveError, Board>")
    override fun isMoveValid(board: Board, square: Square): Board {
        return when (board) {
            is BoardWin, is BoardDraw -> error("Game is over")
            is BoardRun -> {
                require(square !in board.grid) { "Position taken $square" }
                require(board.turn != null) { "Game is running" }
                val moves = board.grid + Move(square, Piece(board.turn.player))
                when {
                    checkWin(board, square) -> BoardWin(moves, board.turn.player)
                    moves.size == config.boardSize.size * config.boardSize.size -> BoardDraw(moves)
                    else -> BoardRun(moves, board.turn.other())
                }
            }
        }
    }

    override fun checkWin(board: Board, square: Square): Boolean {
        require(board.turn != null) { "Game is running" }
        val backSlash = square.row.toIndex() == square.col.toIndex()
        val slash = square.row.toIndex() + square.col.toIndex() == config.boardSize.size - 1
        val places = board.grid.filter { it.value == Piece(board.turn.player) }.keys + square
        return places.count { it.col == square.col } == WINNING_PIECES ||
                places.count { it.row == square.row } == WINNING_PIECES ||
                places.count { backSlash } == WINNING_PIECES ||
                places.count { slash } == WINNING_PIECES ||
                board.turn.timeLeftInSec.value <= 0
    }

    override fun isFinished(board: Board): Boolean = when (board) {
        is BoardWin, is BoardDraw -> true
        is BoardRun -> false
    }

    override fun initialBoard(): Board = BoardRun(emptyMap(), BoardTurn(Player.w, turnTimer))

    override val points: GamePoints
        get() = GamePoints(
            onFinish = GamePointsOnWin(winner = NonNegativeValue(300), loser = NonNegativeValue(100)),
            onDraw = GamePointsOnDraw(shared = NonNegativeValue(150)),
            onForfeitOrTimer = GamePointsOnForfeitOrTimer(
                winner = NonNegativeValue(400),
                forfeiter = NonNegativeValue(0)
            )
        )
    override val turnTimer: NonNegativeValue
        get() = NonNegativeValue(60)
}