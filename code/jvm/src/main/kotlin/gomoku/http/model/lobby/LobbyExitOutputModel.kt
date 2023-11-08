package gomoku.http.model.lobby

data class LobbyExitOutputModel(val lobbyId: Int, val message: String = "Lobby was exited successfully.")
