package gomoku.domain.game.board.moves.square

import gomoku.domain.components.Component
import gomoku.domain.components.GettingRowResult
import gomoku.domain.components.RowError
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that represents a row in a board.
 */
class Row private constructor(val number: Int) : Component, Indexable {

    companion object {
        operator fun invoke(value: Int): GettingRowResult {
            return if (value > 0) {
                Success(Row(value))
            } else {
                Failure(RowError.InvalidRow(value))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Row) return false

        if (number != other.number) return false

        return true
    }

    override fun toString(): String = number.toString()

    override fun hashCode(): Int = number

    override fun toIndex(): Int = number - 1
}
