package gomoku.repository.jdbi.model.lobby

import gomoku.domain.Id
import gomoku.domain.lobby.Lobby
import gomoku.repository.jdbi.model.JdbiModel
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiLobbyModel(
    val id: Int,
    @ColumnName("host_id")
    val hostId: Int,
    @ColumnName("variant_id")
    val variantId: Int
) : JdbiModel<Lobby> {
    override fun toDomainModel(): Lobby {
        return Lobby(
            lobbyId = Id(id),
            userId = Id(hostId),
            variantId = Id(variantId)
        )
    }
}
