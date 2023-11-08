package gomoku.domain.lobby

import gomoku.domain.components.Id

/**
 * Represents a game lobby.
 * @param lobbyId The lobby's unique identifier.
 * @param userId The user's unique identifier.
 * @param variantId The variant's unique identifier.
 */
data class Lobby(
    val lobbyId: Id,
    val userId: Id,
    val variantId: Id
)
