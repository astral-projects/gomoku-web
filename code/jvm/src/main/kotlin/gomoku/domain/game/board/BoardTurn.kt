package gomoku.domain.game.board

data class BoardTurn(
    val player: Player,
    val timeLeftInSec: Int
) {
    init {
        require(timeLeftInSec >= 0) { "Time left must be non-negative" }
    }

}