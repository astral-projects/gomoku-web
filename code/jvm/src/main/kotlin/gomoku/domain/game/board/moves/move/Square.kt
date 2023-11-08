package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row

/**
 * Represents square on the board, indicated by a column and a row.
 * @property col The column on the board where the square is located.
 * @property row The row on the board where the square is located.
 */
data class Square(val col: Column, val row: Row) {

    companion object {
        const val SEPARATOR = "-"
    }

    override fun toString(): String = "$col$SEPARATOR$row"
}
