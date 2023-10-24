package gomoku.repository

import gomoku.domain.NonNegativeValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.GamePointsOnDraw
import gomoku.domain.game.GamePointsOnForfeitOrTimer
import gomoku.domain.game.GamePointsOnWin
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.Variant
import gomoku.domain.game.variant.VariantConfig
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantName
import gomoku.utils.get

class TestVariant : Variant {
    override val config = VariantConfig(
        name = VariantName.TEST,
        openingRule = OpeningRule.PRO,
        boardSize = BoardSize.FIFTEEN,
    )
    override val points: GamePoints
        get() = GamePoints(
            onFinish = GamePointsOnWin(
                winner = NonNegativeValue(10).get(),
                loser = NonNegativeValue(5).get()
            ),
            onDraw = GamePointsOnDraw(
                shared = NonNegativeValue(5).get()
            ),
            onForfeitOrTimer = GamePointsOnForfeitOrTimer(
                winner = NonNegativeValue(10).get(),
                forfeiter = NonNegativeValue(0).get()
            )
        )
    override val turnTimer: NonNegativeValue
        get() = NonNegativeValue(10).get()

    override fun isMoveValid(board: Board, square: Square): Board {
        val turn = board.turn ?: return board
        return BoardRun(
            moves = board.grid + Move(square, Piece(turn.player)),
            turn = turn.other()
        )
    }

    override fun checkWin(board: Board, square: Square): Boolean {
        return true
    }

    override fun isFinished(board: Board): Boolean {
        return board.grid.isNotEmpty()
    }

    override fun initialBoard(): Board {
        return BoardRun(
            moves = emptyMap(),
            turn = BoardTurn(
                player = Player.w,
                timeLeftInSec = turnTimer
            )
        )
    }
}