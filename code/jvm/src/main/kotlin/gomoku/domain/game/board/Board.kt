package gomoku.domain.game.board

import gomoku.domain.game.board.errors.BoardMakeMoveResult
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.Variant

/**
 * Represents a game board.
 * @property grid The grid of the game board.
 * @property turn The turn of the game board. Depending on the game state, it can be null.
 */
sealed class Board(val grid: Moves, val turn: BoardTurn?) {
    override fun toString(): String {
        return "Board(grid=$grid, turn=$turn)"
    }
}
class BoardRun(moves: Moves, turn: BoardTurn) : Board(moves, turn)
class BoardWin(moves: Moves, val winner: Player) : Board(moves, null)
class BoardDraw(moves: Moves) : Board(moves, null)

/**
 * Checks if a move is valid on the board, depending on received variant rules.
 * @param variant The variant of the game.
 * @param player The player who is making the move.
 * @param toSquare The square where the move is being made.
 */
fun Board.play(variant: Variant, player: Player, toSquare: Square): BoardMakeMoveResult =
    variant.isMoveValid(this, player, toSquare)

/**
 * Checks if the game is finished based on the board state.
 */
fun Board.isFinished(): Boolean = this is BoardWin || this is BoardDraw
