package gomoku.domain.game.variant

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.errors.BoardMakeMoveResult
import gomoku.domain.game.variant.config.VariantConfig

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
     * Check if a move on the given board is valid according to the variant rules.
     * @param board The game board.
     * @param square The square where the move is being made.
     * @return The updated game board if the move is valid, or null if the move is invalid.
     */
    fun isMoveValid(board: Board, square: Square): BoardMakeMoveResult

    /**
     * Check if the game is won based on the last move made.
     * @param board The game board that is currently being played.
     * @param square The square where the last move was made.
     * @return true if the game is won, false otherwise.
     */
    fun checkWin(board: Board, square: Square): Boolean

    /**
     * Gets the initial game board for this variant.
     * @return The initial game board.
     */
    fun initialBoard(): Board
}
