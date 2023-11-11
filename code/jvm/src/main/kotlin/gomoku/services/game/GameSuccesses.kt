package gomoku.services.game

import gomoku.domain.components.Id

/**
 * Represents a successful operation result when finding a game.
 * Either a game was found, and the player joined it, or a lobby was created and the player is waiting for an opponent.
 * @param id The id of the game created or the lobby created.
 * @param message A message describing the result.
 */
sealed class FindGameSuccess(val id: Id, val message: String) {
    class GameMatch(gameId: Id) : FindGameSuccess(
        gameId,
        "Joined the game successfully with id=${gameId.value}"
    )

    class LobbyCreated(lobbyId: Id) : FindGameSuccess(
        lobbyId,
        "Lobby created successfully with id=${lobbyId.value}"
    )

    class StillInLobby(lobbyId: Id) : FindGameSuccess(
        lobbyId,
        "Still waiting in the lobby with id=${lobbyId.value}"
    )
}

/**
 * Represents a successful operation result when waiting for a game.
 * Either a game was found or the user is still waiting in a lobby.
 * @param id The id of the game created or the lobby created.
 * @param message A message describing the result.
 */

sealed class WaitForGameSuccess(val id: Id, val message: String) {
    class GameMatch(gameId: Id) : WaitForGameSuccess(
        gameId,
        "Joined the game successfully with id=${gameId.value}"
    )

    class WaitingInLobby(lobbyId: Id) : WaitForGameSuccess(
        lobbyId,
        "Waiting in lobby with id=${lobbyId.value}"
    )
}
