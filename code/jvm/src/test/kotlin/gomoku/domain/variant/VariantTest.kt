package gomoku.domain.variant

import gomoku.domain.game.board.Board
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.variant.Variant
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.get
import java.util.*

/**
 * Provides a set of test functions that should be implemented for all variant implementations,
 * and a set of helper functions to facilitate in that process.
 */
abstract class VariantTest {

    abstract val variant: Variant
    abstract fun `can make moves on the board`()
    abstract fun `can detect a diagonal slash win`()
    abstract fun `can detect a diagonal backslash win`()
    abstract fun `can detect a horizontal win`()
    abstract fun `can detect a vertical win`()
    abstract fun `can detect a draw`()

    companion object {

        /**
         * Makes a list of moves on the board, according to the given variant,
         * and resulting board.
         * @param variant The variant of the game.
         * @param moves The list of moves to make.
         * @throws IllegalArgumentException if any of the moves is invalid for the given variant, or
         * if the game is over.
         */
        @Throws(IllegalArgumentException::class)
        fun makeMoves(variant: Variant, moves: List<Square>): Board {
            val initialBoard: Board = variant.initialBoard()
            val boardTurn = initialBoard.turn
            requireNotNull(boardTurn) { "Board turn cannot be null" }
            return moves.foldIndexed(variant.initialBoard()) { idx: Int, board: Board, move: Square ->
                println(
                    "Board state: ${board::class.java.simpleName}\n" +
                        "Making Move[${idx + 1}] to: Square($move)[Column(${move.col.toIndex()})-Row(${move.row.toIndex()})]"
                )
                val observedBoardTurn = board.turn
                requireNotNull(observedBoardTurn) { "Board turn cannot be null" }
                when (val moveResult = variant.isMoveValid(board, observedBoardTurn.player, move)) {
                    is Failure -> {
                        val errorMessage = when (moveResult.value) {
                            is MakeMoveError.GameOver -> "Game is over"
                            is MakeMoveError.InvalidPosition -> "Invalid position"
                            is MakeMoveError.NotYourTurn -> "Not your turn"
                            is MakeMoveError.PositionTaken -> "Position taken"
                        }
                        throw IllegalArgumentException(errorMessage)
                    }

                    is Success ->
                        moveResult.value
                            .also { it.print(variant.config) }
                }
            }
        }

        /**
         * Returns a list of all possible squares in a board, given its size.
         * @param boardSize The board size.
         * @return A list of all possible squares from the given board size.
         */
        fun possibleSquaresIn(boardSize: Int): List<Square> {
            val range = 0 until boardSize - 1
            val allRows: List<Row> = (range).map { Row(index = it).get() }
            val allCols: List<Column> = (range).map { Column(index = it).get() }
            return allRows.fold(emptyList()) { acc, row ->
                acc + allCols.map { col -> Square(col, row) }
            }
        }

        /**
         * Returns a list of all possible squares the maximum board size can have.
         */
        fun maximumBoardSizeSquares(): List<Square> {
            val range = 0..Column.MAX_INDEX
            val allRows: List<Row> = (range).map { Row(index = it).get() }
            val allCols: List<Column> = (range).map { Column(index = it).get() }
            return allRows.fold(emptyList()) { acc, row ->
                acc + allCols.map { col -> Square(col, row) }
            }
        }

        /**
         * Returns a list of all possible squares outside a board, given its size.
         * @param boardSize The board size.
         */
        fun possibleSquaresOutside(boardSize: Int): List<Square> {
            val maximumBoardSizeSquares = maximumBoardSizeSquares().toSet()
            val possibleSquaresIn = possibleSquaresIn(boardSize).toSet()
            return maximumBoardSizeSquares.minus(possibleSquaresIn).toList()
        }

        /**
         * Returns a list of all center squares in a board, given its size.
         * @param boardSize The board size.
         */
        fun getCenterSquares(boardSize: Int): List<Square> {
            val centerIndex = boardSize / 2
            return if (boardSize % 2 == 0) {
                // For even board sizes, there is only one center square
                listOf(Square(centerIndex - 1, centerIndex - 1))
            } else {
                // For odd board sizes, there are four center squares
                listOf(
                    Square(centerIndex - 1, centerIndex - 1),
                    Square(centerIndex - 1, centerIndex),
                    Square(centerIndex, centerIndex - 1),
                    Square(centerIndex, centerIndex)
                )
            }
        }

        /**
         * Prints the board to the console, according to the given variant configuration.
         *
         * Example:
         *
         * ```kotlin
         *         Turn = B
         *             a   b   c   d   e
         *         .---+---+---+---+---+---.
         *         |   |   |   |   |   |   |
         *       1 |--[B]-[W]--|---|--[W]--|
         *         |   |   |   |   |   |   |
         *       2 |--[W]--|---|---|---|---|
         *         |   |   |   |   |   |   |
         *       3 |---|---|---|--[B]--|---|
         *         |   |   |   |   |   |   |
         *       4 |---|---|---|---|---|---|
         *         |   |   |   |   |   |   |
         *       5 |--[B]--|---|---|---|---|
         *         |   |   |   |   |   |   |
         *         .---+---+---+---+---+---.
         * ```
         * @param variantConfig The variant configuration.
         */
        private fun Board.print(variantConfig: VariantConfig) {
            val dim = variantConfig.boardSize.size
            val topBottomBorder = "." + "---+".repeat(dim - 1) + "---."
            val middleBorder = "|" + "   |".repeat(dim)
            val tabValue = 5
            val nrOfColumns = dim - 1
            val tab = " ".repeat(tabValue)
            val boardTurn = turn
            val turnTemplate = if (boardTurn != null) "Turn was = ${boardTurn.other().player}" else ""
            println("\n" + tab + turnTemplate)
                .also {
                    print("$tab   ")
                        .also {
                            ('a'..'z')
                                .take(nrOfColumns)
                                .forEach { print(" $it  ") }
                        }
                        .also { println() }
                }
            println(tab + topBottomBorder)
            for (row in 0 until dim - 1) {
                println(tab + middleBorder)
                val factor = if (row < 9) 2 else 3
                print(" ".repeat(tabValue - factor) + "${row + 1} |-")
                for (col in 0 until nrOfColumns) {
                    val square = Square(Column(index = col).get(), Row(index = row).get())
                    val piece = grid[square]
                    print("-")
                    val pieceString = if (piece != null) {
                        "[${piece.player}]"
                    } else {
                        "-|-"
                    }
                    print(pieceString)
                }
                println("--|")
            }
            println(tab + middleBorder)
            println(tab + topBottomBorder + "\n")
        }

        /**
         * Retrieves a sequence of squares that would result in a draw, according to the given variant
         * configuration.
         * The sequence generated will work for both even and odd board sizes, since with odd the vertical
         * win will be blocked, and with even the slash and backslash wins will be blocked.
         * @param variant The variant of the game.
         * @return A list of squares that would result in a draw.
         */
        fun getDrawSquareSequence(
            variant: Variant
        ): List<Square> {
            val boardSize = variant.config.boardSize.size
            val allSquaresInBoard = possibleSquaresIn(boardSize)
            val rowGroupSize = variant.winningPieces - 1
            var shouldRotateRowGroup = false
            var rowIndex = 0
            while (rowIndex < boardSize - 1) {
                val isInRowGroupStart = rowIndex % (rowGroupSize) == 0
                val remainingRows = boardSize - rowIndex - 1
                val numberOfRowsToRotate = rowGroupSize.coerceAtMost(remainingRows)
                if (rowIndex != 0 && isInRowGroupStart) {
                    // Toggle rotation status for the next row group
                    shouldRotateRowGroup = !shouldRotateRowGroup
                }
                if (shouldRotateRowGroup) {
                    repeat(numberOfRowsToRotate) {
                        val rowStartIndex = rowIndex * (boardSize - 1)
                        val rowEndIndex = rowStartIndex + (boardSize - 1)
                        // Rotate the row one position to the right
                        val extractedRow = allSquaresInBoard.subList(rowStartIndex, rowEndIndex)
                        Collections.rotate(extractedRow, 1)
                        rowIndex += 1
                    }
                } else {
                    // Skip the next row group
                    rowIndex += rowGroupSize
                }
            }
            return allSquaresInBoard
        }
    }
}
