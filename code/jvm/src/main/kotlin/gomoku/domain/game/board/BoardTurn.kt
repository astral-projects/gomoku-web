package gomoku.domain.game.board

import gomoku.domain.NonNegativeValue

data class BoardTurn(
    val player: Player,
    val timeLeftInSec: NonNegativeValue
) {
    fun other(): BoardTurn = when (player) {
        Player.W -> BoardTurn(Player.B, timeLeftInSec)
        Player.B -> BoardTurn(Player.W, timeLeftInSec)
    }
}
