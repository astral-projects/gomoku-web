package gomoku.domain.game.board

import gomoku.domain.components.NonNegativeValue

/**
 * Represents a turn of a player on a board.
 * @property player The player who has the turn.
 * @property timeLeftInSec The time left for the player to make a play.
 */
data class BoardTurn(
    val player: Player,
    val timeLeftInSec: NonNegativeValue
) {
    fun other(): BoardTurn = when (player) {
        Player.W -> BoardTurn(Player.B, timeLeftInSec)
        Player.B -> BoardTurn(Player.W, timeLeftInSec)
    }
}
