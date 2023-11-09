package gomoku.domain.game.board.moves.move

import gomoku.domain.game.board.Player

/**
 * Represents a piece on a board.
 * @property player The player who owns the piece.
 */
data class Piece(val player: Player)
