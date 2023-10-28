package gomoku.http.model.game

data class GameMoveOutputModel(
    val gameId: Int,
    val message: String = "Move was made successfully."
)
