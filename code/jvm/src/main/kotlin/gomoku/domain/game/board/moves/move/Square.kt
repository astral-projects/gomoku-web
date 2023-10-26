package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row

/**
 * Represents a combination defined by a unique [col] and [row] on the board.
 */
data class Square(val col: Column, val row: Row) {

    companion object {
        const val SEPARATOR = "-"
    }

    override fun toString(): String = "$col$SEPARATOR$row"
}
