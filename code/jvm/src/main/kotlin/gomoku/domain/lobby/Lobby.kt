package gomoku.domain.lobby

import gomoku.domain.Id
import gomoku.domain.SerializableDomainModel

data class Lobby(
    val lobbyId: Id,
    val userId: Id,
    val variantId: Id,
): SerializableDomainModel