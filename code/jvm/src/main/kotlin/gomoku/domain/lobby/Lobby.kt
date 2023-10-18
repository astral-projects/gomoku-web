package gomoku.domain.lobby

import gomoku.domain.Id

data class Lobby(
    val lobbyId: Id,
    val userId: Id,
    val variantId: Id,
)