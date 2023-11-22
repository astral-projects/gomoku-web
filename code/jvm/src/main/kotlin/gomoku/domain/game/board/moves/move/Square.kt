package gomoku.domain.game.board.moves.move

import gomoku.domain.components.PositiveValue
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.utils.get
import kotlin.math.abs

/**
 * Represents square on the board, which is the intersection of a [Column] and a [Row].
 * @property col The column on the board where the square is located.
 * @property row The row on the board where the square is located.
 */
data class Square(val col: Column, val row: Row) {

    companion object {
        operator fun invoke(value: String): Square =
            Square(Column(value = value[0]).get(), Row(index = value.drop(1).toInt() - 1).get())

        operator fun invoke(colIndex: Int, rowIndex: Int): Square =
            Square(Column(index = colIndex).get(), Row(index = rowIndex).get())
    }

    override fun toString(): String = "$col$row"

    /**
     * Checks if a square is N intersections apart from another square, including it.
     *
     * Example:
     * For two squares to be considered 2 intersections apart, then the row and column differences
     * (as absolute values) between them should be one of the following pairs:
     *
     * ```kotlin
     * rowDiff = 3, colDiff = 0
     * rowDiff = 3, colDiff = 1
     * rowDiff = 3, colDiff = 2
     * rowDiff = 3, colDiff = 3
     * rowDiff = 2, colDiff = 3
     * rowDiff = 1, colDiff = 3
     * rowDiff = 0, colDiff = 3
     * ```
     * @param toSquare The square to whom the distance is being calculated.
     * @param targetIntersectionsValue The number of intersections that the squares should be apart.
     * @return true if the squares are N intersections apart, false otherwise.
     */
    fun isNIntersectionsApartFrom(toSquare: Square, targetIntersectionsValue: PositiveValue): Boolean {
        val rowDiff = abs(row.toIndex() - toSquare.row.toIndex())
        val colDiff = abs(col.toIndex() - toSquare.col.toIndex())
        val targetIntersections = targetIntersectionsValue.value
        for (i in targetIntersections downTo 0) {
            if (i == targetIntersections) {
                for (j in 0..targetIntersections) {
                    if (rowDiff == i && colDiff == j) {
                        return true
                    }
                }
            }
            if (rowDiff == i && colDiff == targetIntersections) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if a square is in the same row as another square.
     * @param other The other square to compare with.
     */
    fun isInSameRow(other: Square): Boolean = this.row == other.row

    /**
     * Checks if a square is in the same column as another square.
     * @param other The other square to compare with.
     */
    fun isInsameColumn(other: Square): Boolean = this.col == other.col

    /**
     * Checks if a square is in the same backslash diagonal as another square.
     * It's the diagonal from the top left to the bottom right.
     * @param other The other square to compare with.
     */
    fun isInSameBackSlash(other: Square): Boolean =
        this.row.toIndex() + this.col.toIndex() == other.row.toIndex() + other.col.toIndex()

    /**
     * Checks if a square is in the same slash diagonal as another square.
     * It's the diagonal from the top right to the bottom left.
     * @param other The other square to compare with.
     */
    fun isInSameSlash(other: Square): Boolean =
        this.row.toIndex() - this.col.toIndex() == other.row.toIndex() - other.col.toIndex()
}
