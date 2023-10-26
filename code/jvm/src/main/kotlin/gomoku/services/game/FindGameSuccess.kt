package gomoku.services.game

import gomoku.domain.components.Id

/**
 * Represents a successful operation result when finding a game.
 * Either a game was found or a lobby was created.
 * @param id The id of the game created or the lobby created.
 * @param message A message describing the result.
 */

sealed class FindGameSuccess(val id: Id, val message: String) {
    class GameMatch(gameId: Id) : FindGameSuccess(
        gameId,
        "Joined the game successfully with the id=${gameId.value}"
    )

    class LobbyCreated(lobbyId: Id) : FindGameSuccess(
        lobbyId,
        "Lobby created successfully with the id=${lobbyId.value}"
    )

    class StillInLobby(lobbyId: Id) : FindGameSuccess(
        lobbyId,
        "Still waiting in the lobby with the id=${lobbyId.value}"
    )
}
