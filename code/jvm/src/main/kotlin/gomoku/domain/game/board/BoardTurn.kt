package gomoku.domain.game.board

data class BoardTurn(
    val player: Player,
    val timeLeftInSec: Int
) {
    init {
        require(timeLeftInSec >= 0) { "Time left must be non-negative" }
    }

    fun other(): BoardTurn = when (player) {
        Player.w -> BoardTurn(Player.b, timeLeftInSec)
        Player.b -> BoardTurn(Player.w, timeLeftInSec)
    }

}