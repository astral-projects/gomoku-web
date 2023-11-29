package gomoku.http.model.game

data class GameExitOutputModel(val userId: Int, val gameId: Int, val message: String = "User with id <$userId> left the Game with id <$gameId>.")
