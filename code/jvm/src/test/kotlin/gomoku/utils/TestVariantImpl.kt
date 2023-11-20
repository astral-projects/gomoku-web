package gomoku.utils

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
import gomoku.domain.game.board.errors.BoardMakeMoveResult
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.Variant
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.domain.game.variant.config.VariantName

/**
 * Variant used for testing purposes.
 * Enables to test the game variant logic without the need to play the whole game.
 * - **To draw**: make two valid moves in the same row (horizontal).
 * - **To win**: the board must have three valid moves.
 *
 * It is not marked as **@Component** because it is not to be used in production.
 */
object TestVariantImpl : Variant {
    override val config = VariantConfig(
        name = VariantName.TEST,
        openingRule = OpeningRule.PRO,
        boardSize = BoardSize.FIFTEEN
    )
    override val points: GamePoints
        get() = GamePoints(
            onFinish = GamePointsOnWin(
                winner = NonNegativeValue(500).get(),
                loser = NonNegativeValue(100).get()
            ),
            onDraw = GamePointsOnDraw(
                shared = NonNegativeValue(250).get()
            ),
            onForfeitOrTimer = GamePointsOnForfeitOrTimer(
                winner = NonNegativeValue(300).get(),
                forfeiter = NonNegativeValue(50).get()
            )
        )
    override val turnTimer: NonNegativeValue
        get() = NonNegativeValue(30).get()

    override fun isMoveValid(board: Board, player: Player, toSquare: Square): BoardMakeMoveResult {
        val turn = board.turn ?: return Success(board)
        if (board.grid.containsKey(toSquare)) {
            return Failure(MakeMoveError.PositionTaken(toSquare))
        }
        if (toSquare.col.toIndex() >= config.boardSize.size || toSquare.row.toIndex() >= config.boardSize.size) {
            return Failure(MakeMoveError.InvalidPosition(toSquare))
        }
        val newMoves = board.grid + Move(toSquare, Piece(turn.player))
        return when {
            checkWin(newMoves) -> Success(BoardWin(newMoves, turn.player))
            checkDraw(newMoves) -> Success(BoardDraw(newMoves))
            else -> Success(BoardRun(newMoves, turn.other()))
        }

    }

    private fun checkWin(newMoves: Moves): Boolean = newMoves.size == 3

    override fun checkDraw(updatedMoves: Moves): Boolean =
        updatedMoves.keys.groupBy { it.row }.map { it.value.size }.any { it >= 2 }

    override fun initialBoard(): Board {
        return BoardRun(
            moves = emptyMap(),
            turn = BoardTurn(
                player = Player.W,
                timeLeftInSec = turnTimer
            )
        )
    }
}
