package gomoku.domain.game.board.moves.square

import gomoku.domain.components.ColumnError
import gomoku.domain.components.Component
import gomoku.domain.components.GettingColumnResult
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that represents a column in a board.
 */
class Column private constructor(val letter: Char) : Component, Indexable {

    companion object {
        operator fun invoke(value: Char): GettingColumnResult {
            return if (value in 'a'..'z') {
                Success(Column(value))
            } else {
                Failure(ColumnError.InvalidColumn(value))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Column) return false

        if (letter != other.letter) return false

        return true
    }

    override fun hashCode(): Int = letter.hashCode()

    override fun toString(): String = letter.toString()

    override fun toIndex(): Int = letter - 'a'
}
