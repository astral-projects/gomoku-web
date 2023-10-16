package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row

/**
 * Represents a square instance defined by a combination of a [Column] and [Row].
 */
data class Square(val col: Column, val row: Row)
fun toSquare(s: String):Square{
    val parts = s.split("-")
    val position = parts[0]
    val player = parts[1]

    val row = position[0].toInt()
    val column = position.substring(1).toCharArray()[0]

    return Square(Column(column), Row(row))
}


