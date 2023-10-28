package gomoku.http.model.game

data class GameExitOutputModel(val gameId: Int, val message: String = "Game was exited successfully.")
