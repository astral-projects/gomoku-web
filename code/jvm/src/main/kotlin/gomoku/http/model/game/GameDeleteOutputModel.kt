package gomoku.http.model.game

data class GameDeleteOutputModel(
    val gameId: Int,
    val message: String = "Game deleted successfully"
)