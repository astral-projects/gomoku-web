package gomoku.domain.game.board

import gomoku.domain.NonNegativeValue

data class BoardTurn(
    val player: Player,
    val timeLeftInSec: NonNegativeValue
) {
    fun other(): BoardTurn = when (player) {
        Player.w -> BoardTurn(Player.b, timeLeftInSec)
        Player.b -> BoardTurn(Player.w, timeLeftInSec)
    }
}
