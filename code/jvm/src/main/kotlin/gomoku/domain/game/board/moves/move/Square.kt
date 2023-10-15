package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row

/**
 * Represents a square instance defined by a combination of a [Column] and [Row].
 */
data class Square(val col: Column, val row: Row)