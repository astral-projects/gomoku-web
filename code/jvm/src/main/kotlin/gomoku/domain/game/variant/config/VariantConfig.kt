package gomoku.domain.game.variant.config

import gomoku.domain.game.board.moves.move.Square

/**
 * Represents a game variant configuration.
 * @property name The name of the game variant.
 * @property openingRule The opening rule of the game variant.
 * @property boardSize The board size of the game variant.
 */
data class VariantConfig(
    val name: VariantName,
    val openingRule: OpeningRule,
    val boardSize: BoardSize
) {

    /**
     * Checks if a square is in bounds of the board.
     * @param square The square to check.
     */
    fun isSquareInBounds(square: Square): Boolean =
        // -1 because the index starts at 0 and the board size starts at 1
        square.row.toIndex() < boardSize.size - 1 && square.col.toIndex() < boardSize.size - 1

    /**
     * Checks if a square is in the center of the board.
     * @param square The square to check.
     */
    fun isSquareInCenter(square: Square): Boolean {
        val centerIndex = boardSize.size / 2
        val rowIndex = square.row.toIndex()
        val columnIndex = square.col.toIndex()
        return if (boardSize.size % 2 == 0) {
            rowIndex == centerIndex - 1 && columnIndex == centerIndex - 1
        } else {
            val upperLeftCorner =
                rowIndex == centerIndex - 1 &&
                    columnIndex == centerIndex - 1
            val upperRightCorner =
                rowIndex == centerIndex - 1 && columnIndex == centerIndex
            val lowerLeftCorner =
                rowIndex == centerIndex && columnIndex == centerIndex - 1
            val lowerRightCorner =
                rowIndex == centerIndex && columnIndex == centerIndex
            // the square is in the center if it is in one of the center intersections, since
            // the board size is odd
            upperLeftCorner || upperRightCorner || lowerLeftCorner || lowerRightCorner
        }
    }
}
