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
        operator fun invoke(index: Int): GettingRowResult {
            return if (index >= 0) {
                Success(Row(index + 1))
            } else {
                Failure(RowError.InvalidRow(index))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Row) return false

        if (number != other.number) return false

        return true
    }

    override fun toString(): String = "$number"

    override fun hashCode(): Int = number

    override fun toIndex(): Int = number - 1
}
