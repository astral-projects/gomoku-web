package gomoku.domain.game.board

data class BoardTurn(
    val turn: Player,
    val timeLeftInSeconds: Int
)