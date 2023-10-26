package gomoku.http.model.game

data class JoinedGameWithSuccessOutputModel(
    val id: String,
    val message: String = "Joined the game successfully with the id=  $id"
)
