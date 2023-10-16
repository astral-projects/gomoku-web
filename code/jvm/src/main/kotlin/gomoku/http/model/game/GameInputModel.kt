package gomoku.http.model.game

data class GameInputModel(
    val gameVariant: String,
    val openingRule: String,
    val boardSize: Int,
)
