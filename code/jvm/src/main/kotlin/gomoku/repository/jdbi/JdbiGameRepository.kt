package gomoku.repository.jdbi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.initialBoard
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.lobby.Lobby
import gomoku.repository.GamesRepository
import gomoku.repository.jdbi.model.JdbiIdModel
import gomoku.repository.jdbi.model.game.JdbiBoardDrawModel
import gomoku.repository.jdbi.model.game.JdbiBoardRunModel
import gomoku.repository.jdbi.model.game.JdbiBoardWinModel
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

    override fun addUserToLobby(variantId: Id, userId: Id): Id? =
        handle.createUpdate(
            """
                insert into dbo.Lobbies (host_id, variant_id)
                values (:host_id, :variant_id)
           """.trimIndent()
        )
            .bind("host_id", userId.value)
            .bind("variant_id", variantId.value)
            .executeAndReturnGeneratedKeys()
            .mapTo<JdbiIdModel>()
            .single()?.toDomainModel()

    override fun waitingInLobby(userId: Id): Lobby? =
        handle.createQuery("select * from dbo.Lobbies where host_id = :userId")
            .bind("userId", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull()?.toDomainModel()

    override fun deleteGame(gameId: Id, userId: Id): Boolean =
        handle.createUpdate("delete from dbo.Games where id = :gameId and host_id = :hostId")
            .bind("gameId", gameId.value)
            .bind("hostId", userId.value)
            .execute() == 1

    override fun userBelongsToTheGame(userId: Id, gameId: Id): Boolean {
        val query =
            handle.createQuery("SELECT * FROM dbo.Games WHERE id = :gameId AND (host_id = :userId OR guest_id = :userId)")
                .bind("gameId", gameId.value)
                .bind("userId", userId.value)
        // TODO("remove findOnly and use mapTo<JdbiModels..> instead")
        val game = query.mapToMap().findOnly()
        return game != null
    }

    override fun userIsTheHost(userId: Id, gameId: Id): Boolean {
        val query =
            handle.createQuery("SELECT * FROM dbo.Games WHERE id = :gameId AND host_id = :userId")
                .bind("gameId", gameId.value)
                .bind("userId", userId.value)
        // TODO("remove findOnly and use mapTo<JdbiModels..> instead")
        val game = query.mapToMap().findOnly()
        return game != null
    }


    override fun getSystemInfo() = SystemInfo

    //TODO: Review this method
    override fun updateGame(gameId: Id, board: Board): Boolean =
        handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET board = :board::jsonb, updated_at = extract(epoch from now()) 
        WHERE id = :gameId;
    """
        ).bind("gameId", gameId.value)
            .bind("board", convertBoardToJdbiJsonString(board)).execute() > 0

    override fun exitGame(gameId: Id, userId: Id): Id? =
        handle.createQuery(
            """
        UPDATE dbo.Games 
        SET state = 'FINISHED'
        WHERE id = :id AND (host_id = :userId OR guest_id = :userId)
        RETURNING 
            CASE
                WHEN host_id != :userId THEN host_id
                WHEN guest_id != :userId THEN guest_id
            END as id
        """
        ).bind("id", gameId.value)
            .bind("userId", userId.value)
            .mapTo<JdbiIdModel>()
            .singleOrNull()?.toDomainModel()

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
    override fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id): Id? =
        handle.createUpdate("insert into dbo.Games (state, board, variant_id, host_id, guest_id, lobby_id) values (:state, CAST(:board AS jsonb), :variant_id, :host_id, :guest_id, :lobby_id)")
            .bind("variant_id", variantId.value)
            .bind("host_id", hostId.value)
            .bind("guest_id", guestId.value)
            .bind("state", "IN_PROGRESS")
            .bind("board", convertBoardToJdbiJsonString(initialBoard()))
            .bind("lobby_id", lobbyId.value)
            .executeAndReturnGeneratedKeys()
            .mapTo<JdbiIdModel>()
            .singleOrNull()?.toDomainModel()

    override fun deleteUserFromLobby(lobbyId: Id): Boolean =
        handle.createUpdate("delete from dbo.Lobbies where id = :lobbyId")
            .bind("lobbyId", lobbyId.value)
            .execute() == 1

    //TODO(It needs do be Implemented if its araw maybe create class that represents the statics,example class draw nobody gets a win ,
    // but the win class the winner gets a win )
    override fun updatePoints(
        gameId: Id,
        winnerId: Id,
        loserId: Id,
        winnerPoints: PositiveValue,
        loserPoints: PositiveValue,
        gamePoint: NonNegativeValue
    ): Boolean =
        handle.createUpdate(
            """
        UPDATE dbo.Statistics
        SET 
            points = points +
                CASE
                    WHEN user_id = :winnerId THEN :winnerPoints
                    WHEN user_id = :loserId THEN :loserPoints
                END,
            games_played = games_played + 1,
            games_won = games_won +  
                CASE
                    WHEN user_id = :winnerId THEN :winPoint
                    ELSE 0
                END
        WHERE user_id IN (:winnerId, :loserId)
        """
        )
            .bind("winnerId", winnerId.value)
            .bind("loserId", loserId.value)
            .bind("winnerPoints", winnerPoints.value)
            .bind("loserPoints", loserPoints.value)
            .bind("winPoint", gamePoint.value)
            .execute() > 0


    override fun getVariantById(variantId: Id): GameVariant? =
        handle.createQuery("select * from dbo.Gamevariants where id = :variantId")
            .bind("variantId", variantId.value)
            .mapTo<JdbiVariantModel>()
            .singleOrNull()?.toDomainModel()

    override fun findIfUserIsInGame(userId: Id): JdbiGameAndVariantModel? =
        handle.createQuery("select * from dbo.Games where state != 'FINISHED' and (host_id = :userId or guest_id = :userId)")
            .bind("userId", userId.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()


    private fun convertBoardToJdbiJsonString(board: Board): String? {
        val jdbiBoardModel = when (board) {
            is BoardWin -> JdbiBoardWinModel(grid = board.grid, winner = board.winner, size = board.size.size)
            is BoardDraw -> JdbiBoardDrawModel(grid = board.grid, size = board.size.size)
            is BoardRun -> JdbiBoardRunModel(
                grid = board.grid,
                turn = board.turn!!,
                size = board.size.size,
            )
        }
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(jdbiBoardModel)
    }
}

