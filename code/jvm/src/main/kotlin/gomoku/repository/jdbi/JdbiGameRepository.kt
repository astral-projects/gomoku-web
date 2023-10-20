package gomoku.repository.jdbi

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Board
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.lobby.Lobby
import gomoku.domain.user.User
import gomoku.repository.GamesRepository
import gomoku.repository.jdbi.model.JdbiIdModel
import gomoku.repository.jdbi.model.game.JdbiBoardRunModel
import gomoku.repository.jdbi.model.game.JdbiGameAndVariantModel
import gomoku.repository.jdbi.model.game.JdbiVariantModel
import gomoku.repository.jdbi.model.lobby.JdbiLobbyModel
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiGameRepository(
    private val handle: Handle
) : GamesRepository {
    override fun getGameById(id: Id): Game? =
        handle.createQuery(
            "select g.id, g.state, g.variant_id as variant_id, g.board, g.created_at, g.updated_at, g.host_id, g.guest_id, gv.name, gv.opening_rule, gv.board_size from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = :id"
        )
            .bind("id", id.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    override fun getVariantById(variantId: Id): GameVariant? {
        return handle.createQuery("select * from dbo.Gamevariants where id = :variantId")
            .bind("variantId", variantId.value)
            .mapTo<JdbiVariantModel>()
            .singleOrNull()?.toDomainModel()
    }


    override fun waitInLobby(variantId: Id, userId: Id): Boolean =
        handle.createUpdate(
            "insert into dbo.Lobbies (host_id, variant_id) " +
                    "values (:host_id, :variant_id)"
        )
            .bind("host_id", userId.value)
            .bind("variant_id", variantId.value)
            .execute() == 1


    override fun checkIfIsLobby(userId: Id): Boolean {
        handle.createQuery("select * from dbo.Lobbies where host_id = :userId")
            .bind("userId", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull() ?: return false
        return true
    }

    override fun deleteGame(gameId: Id, userId: Id): Boolean =
        handle.createUpdate("delete from dbo.Games where id = :gameId and host_id = :hostId")
            .bind("gameId", gameId)
            .bind("hostId", gameId)
            .execute() == 1


    override fun userBelongsToTheGame(userId: Id, gameId: Id): Boolean {
        val query =
            handle.createQuery("SELECT * FROM dbo.Games WHERE id = :gameId AND (host_id = :userId OR guest_id = :userId)")
                .bind("gameId", gameId.value)
                .bind("userId", userId.value)
        val game = query.mapToMap().findOnly()
        return game != null
    }

    override fun userIsTheHost(userId: Id, gameId: Id): Boolean {
        val query =
            handle.createQuery("SELECT * FROM dbo.Games WHERE id = :gameId AND host_id = :userId")
                .bind("gameId", gameId.value)
                .bind("userId", userId.value)
        val game = query.mapToMap().findOnly()
        return game != null
    }

    override fun updatePoints(gameId: Id, userId: Id): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSystemInfo() = SystemInfo

    override fun updateGame(gameId: Id, board: Board): Boolean {
        if (board.turn == null) return false
        val jdbiBoard = JdbiBoardRunModel(board.grid, board.size.size, board.turn)
        val mapper = jacksonObjectMapper()
        val jdbiBoardJson = mapper.writeValueAsString(jdbiBoard)
        val updateQuery = handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET board = :board::jsonb, updated_at = extract(epoch from now()) 
        WHERE id = :gameId;
    """
        ).bind("gameId", gameId.value)
            .bind("board", jdbiBoardJson)
        val rowsUpdated = updateQuery.execute()
        return rowsUpdated > 0
    }

    override fun exitGame(id: Id, userId: Id): Boolean = handle.createUpdate(
        """
        UPDATE dbo.Games 
        SET state = 'FINISHED'
        WHERE id = :id AND (host_id = :userId OR guest_id = :userId)
    """
    )
        .bind("id", id)
        .bind("userId", userId.value)
        .execute() == 1


    override fun getGameStatus(gameId: Id, userId: Id): Game? =
        handle.createQuery("select g.id, g.state, g.variant_id as variant_id, g.board, g.created_at, g.updated_at, g.host_id, g.guest_id, gv.name, gv.opening_rule, gv.board_size from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = :gameId AND (g.host_id = :id OR g.guest_id = :id)")
            .bind("id", userId.value)
            .bind("gameId", gameId.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()


    override fun isMatchmaking(variantId: Id, userId: Id): Lobby? =
        handle.createQuery("select * from dbo.Lobbies where variant_id = :variant_id and host_id != :host_id")
            .bind("variant_id", variantId.value)
            .bind("host_id", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull()?.toDomainModel()

    //TODO(The board isn't being initialize correctly. Review the insertion query)
    override fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id): Id = handle.createUpdate(
        "insert into dbo.Games (state, board, variant_id, host_id, guest_id, lobby_id) values (:state, CAST(:board AS jsonb), :variant_id, :host_id, :guest_id, :lobby_id)"
    ).bind("variant_id", variantId.value)
        .bind("host_id", hostId.value)
        .bind("guest_id", guestId.value)
        .bind("state", "IN_PROGRESS")
        .bind("board", "[]")
        .bind("lobby_id", lobbyId.value)
        .executeAndReturnGeneratedKeys()
        .mapTo<JdbiIdModel>()
        .single().toDomainModel()


    override fun deleteUserFromLobby(userId: Id): Boolean =
        handle.createUpdate("Delete from dbo.Lobbies where host_id = :userId")
            .bind("userId", userId.value)
            .execute() == 1

}
