package gomoku.domain.variant

import gomoku.domain.game.board.Board
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.variant.Variant
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.get

/**
 * Provides a set of tests that should be run for all variants
 * to ensure they are working as expected.
 */
abstract class VariantTest {

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
                            "Move[$idx]: Square($move)[Row(${move.row.toIndex()})-Column(${move.col.toIndex()})]"
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

                    is Success -> moveResult.value
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
    }

}