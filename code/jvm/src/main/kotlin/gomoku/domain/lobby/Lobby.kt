package gomoku.domain.lobby

import gomoku.domain.components.Id

data class Lobby(
    val lobbyId: Id,
    val userId: Id,
    val variantId: Id
)
