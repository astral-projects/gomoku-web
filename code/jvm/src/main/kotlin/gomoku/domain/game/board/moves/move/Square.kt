package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row

/**
 * Represents a square instance defined by a combination of a [Column] and [Row].
 */
data class Square(val col: Column, val row: Row) {
    companion object {
        // TODO("revisit this code")
        fun toSquare(s: String): Square {
            val parts = s.split("-")
            val position = parts[0]
            val player = parts[1]

            val row = position.substring(1).toInt()

            // A coluna é o primeiro caractere da string position
            val column = position[0].toLowerCase()

            return Square(Column(column), Row(row))
        }
    }

}
