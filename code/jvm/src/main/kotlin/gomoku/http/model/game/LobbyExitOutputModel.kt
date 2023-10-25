package gomoku.http.model.game

class LobbyExitOutputModel (val id:Int,
    val message: String = "You exited the lobby with the id=${id}"
)