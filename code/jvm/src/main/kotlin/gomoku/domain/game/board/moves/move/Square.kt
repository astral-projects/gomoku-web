package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import java.util.*

/**
 * Represents a square instance defined by a combination of a [Column] and [Row].
 */
data class Square(val col: Column, val row: Row) {
    companion object {
        fun toSquare(col: String, row: Int): Square {

            return Square(Column(col.toCharArray()[0]), Row(row))
        }
    }
}
