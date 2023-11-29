package gomoku.services.game

/**
 * Represents a successful operation result when finding a game.
 *
 * There are three possible results:
 * - [GameMatch] a game was found, and the player joined it.
 * - [LobbyCreated] a lobby was created, and the player is waiting for a guest.
 * @param id The id of the game or the lobby created/retrieved.
 * @param message A message describing the result.
 */
sealed class FindGameSuccess(val id: Int, val message: String) {
    class GameMatch(gameInt: Int, message: String? = null) : FindGameSuccess(
        gameInt,
        message ?: "Joined the game successfully with id=$gameInt"
    )

    class LobbyCreated(lobbyInt: Int, message: String? = null) : FindGameSuccess(
        lobbyInt,
        message ?: "Lobby created successfully with id=$lobbyInt"
    )
}

/**
 * Represents a successful operation result when waiting for a game.
 *
 * There are two possible results:
 * - [GameMatch] a game was found, and the player joined it.
 * - [WaitingInLobby] the player is still waiting in the lobby.
 * @param id The id of the game created or the lobby created.
 * @param message A message describing the result.
 */

sealed class WaitForGameSuccess(val id: Int, val message: String) {
    class GameMatch(gameInt: Int, message: String? = null) : WaitForGameSuccess(
        gameInt,
        message ?: "Already in game with id <$gameInt>"
    )

    class WaitingInLobby(lobbyInt: Int, message: String? = null) : WaitForGameSuccess(
        lobbyInt,
        message ?: "Waiting in lobby with id <$lobbyInt>"
    )
}
