package gomoku.http.model.lobby

import gomoku.domain.components.Id
import gomoku.http.Rels
import gomoku.http.Uris
import gomoku.http.media.siren.SirenModel
import gomoku.http.media.siren.siren
import gomoku.services.game.WaitForGameSuccess
import org.springframework.http.HttpMethod

class LobbyOutputModels {

    /**
     * Generates a Siren representation for the waiting state in either a game match or a lobby.
     *
     * @param gameWait The state of waiting, either in a game match or a lobby.
     * @return A SirenModel representing the waiting state with appropriate links and actions.
     */
    fun waitingInLobby(
        gameWait: WaitForGameSuccess
    ): SirenModel<WaitForGameSuccess> {
        return when (gameWait) {
            is WaitForGameSuccess.GameMatch ->
                siren(
                    gameWait
                ) {
                    clazz("game")
                    requireAuth()
                    link(Uris.Games.byId(gameWait.id), Rels.SELF)
                    action(
                        name = "Exit Game",
                        href = Uris.Games.byId(gameWait.id),
                        method = HttpMethod.POST,
                        type = "application/json"
                    ) {
                        clazz("exit-game")
                        requireAuth()
                    }
                    action(
                        name = "Make Move",
                        href = Uris.Games.byId(gameWait.id),
                        method = HttpMethod.POST,
                        type = "application/json"
                    ) {
                        clazz("make-move")
                        requireAuth()
                        textField("col")
                        numberField("row")
                    }
                }

            is WaitForGameSuccess.WaitingInLobby ->
                siren(
                    gameWait
                ) {
                    clazz("lobby")
                    requireAuth()
                    link(Uris.Lobby.isInLobby(gameWait.id), Rels.SELF)
                    action(
                        name = "Exit Lobby",
                        href = Uris.Lobby.exitLobby(gameWait.id),
                        method = HttpMethod.DELETE,
                        type = "application/json"
                    ) {
                        clazz("exit-lobby")
                        requireAuth()
                    }
                }
        }
    }

    /**
     * Generates a Siren representation for exiting a lobby.
     *
     * @param lobbyId The ID of the lobby to exit.
     * @return A SirenModel representing the exit from the lobby with appropriate links.
     */
    fun exitLobby(
        lobbyId: Id
    ): SirenModel<LobbyExitOutputModel> =
        siren(
            LobbyExitOutputModel(lobbyId.value)
        ) {
            clazz("lobby-exit")
            requireAuth()
            link(Uris.Lobby.exitLobby(lobbyId.value), Rels.SELF)
        }

    data class LobbyExitOutputModel(
        val lobbyId: Int,
        val message: String = "Lobby was exited successfully."
    )
}
