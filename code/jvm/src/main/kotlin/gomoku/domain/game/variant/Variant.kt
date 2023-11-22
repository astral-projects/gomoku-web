package gomoku.domain.game.variant

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.errors.BoardMakeMoveResult
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.config.VariantConfig

private const val WINNING_PIECES = 5

/**
 * Represents a game variant that defines the rules and characteristics of a game.
 */
interface Variant {

    /**
     * Configuration specific to this game variant.
     */
    val config: VariantConfig

    /**
     * Points system used in the game variant.
     */
    val points: GamePoints

    /**
     * Maximum turn time allowed for players in this variant.
     */
    val turnTimer: NonNegativeValue

    /**
     * Check if a move on the given board is valid, according to the variant rules.
     * @param board The game board.
     * @param player The player who is making the move.
     * @param toSquare The square where the move is being made.
     * @return The updated game board if the move was valid, or an error otherwise.
     */
    fun isMoveValid(board: Board, player: Player, toSquare: Square): BoardMakeMoveResult

    /**
     * Gets the initial game board for this variant.
     * @return The initial game board.
     */
    fun initialBoard(): Board

    /**
     * Checks if the game is a draw, according to this variant rules.
     * @param updatedMoves The updated moves of the game.
     * @return true if the game is a draw, false otherwise.
     */
    fun checkDraw(updatedMoves: Moves): Boolean =
        updatedMoves.size == (config.boardSize.size - 1) * (config.boardSize.size - 1)

    /**
     * Checks if the game is a win, according to this variant rules.
     * Should only be called if the game is still in progress, otherwise
     * it will throw an [IllegalArgumentException].
     * @param board The game board.
     * @param square The square where the move is being made to.
     * @return true if the game is a win, false otherwise.
     * @throws IllegalArgumentException if the game is over, when this method is called.
     */
    @Throws(IllegalArgumentException::class)
    fun checkWin(board: Board, square: Square): Boolean {
        requireNotNull(board.turn) { "Board turn cannot be null" }
        if (board.turn.timeLeftInSec.value <= 0) {
            return true
        }
        val places = board.grid.filter { it.value == Piece(board.turn.player) }.keys + square
        return places.hasConsecutive(WINNING_PIECES, Square::isInSameRow) ||
            places.hasConsecutive(WINNING_PIECES, Square::isInsameColumn) ||
            places.hasConsecutive(WINNING_PIECES, Square::isInSameSlash) ||
            places.hasConsecutive(WINNING_PIECES, Square::isInSameBackSlash)
    }

    /**
     * Checks if a set of squares has consecutive squares, according to the given condition.
     * @param length The length of the consecutive squares to check.
     * @param condition The condition to check for each square.
     */
    private fun Set<Square>.hasConsecutive(
        length: Int,
        condition: (Square, Square) -> Boolean
    ): Boolean {
        if (size < length) {
            return false
        }
        // TODO("is not detecting consecutive squares in the same row, revisit")
        val sortedSquares = sortedBy { it.row.toIndex() }
        for (i in 0 until sortedSquares.size - length + 1) {
            val firstSquare = sortedSquares[i]
            val lastSquare = sortedSquares[i + length - 1]
            if (condition(firstSquare, lastSquare)) {
                return true
            }
        }
        return false
    }
}
