package gomoku.domain.game.variant

import gomoku.domain.NonNegativeValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.moves.move.Square

interface Variant {
    val config: VariantConfig
    val points: GamePoints
    val turnTimer: NonNegativeValue
    fun isMoveValid(board: Board, square: Square): Board
    fun checkWin(board: Board, square: Square): Boolean
    fun isFinished(board: Board): Boolean
    fun initialBoard(): Board
}
