package gomoku.repository.jdbi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.game.Game
import gomoku.domain.game.GameState
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.VariantConfig
import gomoku.domain.game.variant.VariantName
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

    override fun getGameById(gameId: Id): Game? =
        handle.createQuery(
            "select g.id, g.state, g.variant_id as variant_id, g.board, g.created_at, g.updated_at, g.host_id, g.guest_id, gv.name, gv.opening_rule, gv.board_size from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = :id"
        )
            .bind("id", gameId.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    override fun insertVariants(variants: List<VariantConfig>): Boolean {
        val values = mutableListOf<String>()
        for (variant in variants) {
            values.add("('${variant.name}', '${variant.openingRule}', ${variant.boardSize.size})")
        }
        val valuesString = values.joinToString(", ")
        return handle.createUpdate("insert into dbo.gamevariants (name, opening_rule, board_size) values $valuesString on conflict (name) do nothing")
            .execute() == 1
    }

    override fun getVariantByName(variantName: VariantName): Id {
        return handle.createQuery("select id from dbo.GameVariants where name = :variantName")
            .bind("variantName", variantName)
            .mapTo<JdbiIdModel>()
            .single().toDomainModel()
    }

    override fun getVariants(): List<GameVariant> =
        handle.createQuery("select * from dbo.Gamevariants")
            .mapTo<JdbiVariantModel>()
            .map { it.toDomainModel() }
            .toList()

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

    override fun checkIfUserIsInLobby(userId: Id): Lobby? =
        handle.createQuery("select * from dbo.Lobbies where host_id = :userId")
            .bind("userId", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull()?.toDomainModel()

    override fun deleteGame(gameId: Id, userId: Id): Boolean =
        handle.createUpdate("delete from dbo.Games where id = :gameId and host_id = :hostId and state = :state")
            .bind("gameId", gameId.value)
            .bind("hostId", userId.value)
            .bind("state", GameState.FINISHED.name)
            .execute() == 1

    override fun findIfUserIsInGame(userId: Id): Game? =
        handle.createQuery(
            """SELECT g.*, gv.name, gv.opening_rule, gv.board_size FROM dbo.Games AS g JOIN dbo.GameVariants AS gv ON g.variant_id = gv.id  WHERE
                g.state != :state AND (g.host_id = :userId OR g.guest_id = :userId);"""
        )
            .bind("userId", userId.value)
            .bind("state", GameState.FINISHED.name)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    override fun userBelongsToTheGame(userId: Id, gameId: Id): Game? =
        handle.createQuery(
            "SELECT g.*, gv.name, gv.opening_rule, gv.board_size FROM dbo.Games AS g " +
                    "JOIN dbo.GameVariants AS gv ON g.variant_id = gv.id WHERE g.id = :gameId AND (g.host_id = :userId OR g.guest_id = :userId)"
        )
            .bind("gameId", gameId.value)
            .bind("userId", userId.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    override fun userIsTheHost(gameId: Id, userId: Id): Game? =
        handle.createQuery(
            "SELECT g.*, gv.name, gv.opening_rule, gv.board_size FROM dbo.Games AS g " +
                    "JOIN dbo.GameVariants AS gv ON g.variant_id = gv.id WHERE g.id = :gameId AND g.host_id = :userId"
        )
            .bind("gameId", gameId.value)
            .bind("userId", userId.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    override fun updatePoints(
        winnerId: Id,
        loserId: Id,
        winnerPoints: NonNegativeValue,
        loserPoints: NonNegativeValue,
        shouldCountAsGameWin: Boolean
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
                END,
            games_drawn = games_drawn + :gamesDrawnIncrement
        WHERE user_id IN (:winnerId, :loserId)
        """
        )
            .bind("winnerId", winnerId.value)
            .bind("loserId", loserId.value)
            .bind("winnerPoints", winnerPoints.value)
            .bind("loserPoints", loserPoints.value)
            .bind("winPoint", if (shouldCountAsGameWin) 1 else 0)
            .bind("gamesDrawnIncrement", if (shouldCountAsGameWin) 0 else 1)
            .execute() > 0

    override fun updateGame(gameId: Id, board: Board): Boolean {
        val gameState = when (board) {
            is BoardWin -> GameState.FINISHED
            is BoardDraw -> GameState.FINISHED
            is BoardRun -> GameState.IN_PROGRESS
        }
        return handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET board = :board::jsonb, updated_at = extract(epoch from now()), state = :state 
        WHERE id = :gameId;
    """
        )
            .bind("gameId", gameId.value)
            .bind("state", gameState.name)
            .bind("board", convertBoardToJdbiModelInJson(board))
            .execute() > 0
    }

    override fun exitGame(gameId: Id, userId: Id): Id? =
        handle.createQuery(
            """
            UPDATE dbo.Games 
            SET state = 'FINISHED'
            WHERE id = :id AND (host_id = :userId OR guest_id = :userId) AND state = 'IN_PROGRESS'
            RETURNING 
                CASE
                    WHEN host_id != :userId THEN host_id
                    WHEN guest_id != :userId THEN guest_id
                END as id
            """
        )
            .bind("id", gameId.value)
            .bind("userId", userId.value)
            .mapTo<JdbiIdModel>()
            .singleOrNull()?.toDomainModel()

    override fun isMatchmaking(variantId: Id, userId: Id): Lobby? =
        handle.createQuery("select * from dbo.Lobbies where variant_id = :variant_id and host_id != :host_id FOR UPDATE")
            .bind("variant_id", variantId.value)
            .bind("host_id", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull()?.toDomainModel()

    override fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id, board: Board): Id? =
        handle.createUpdate("insert into dbo.Games (state, board, variant_id, host_id, guest_id, lobby_id) values (:state, cast(:board as jsonb), :variant_id, :host_id, :guest_id, :lobby_id)")
            .bind("variant_id", variantId.value)
            .bind("host_id", hostId.value)
            .bind("guest_id", guestId.value)
            .bind("state", GameState.IN_PROGRESS.name)
            .bind("board", convertBoardToJdbiModelInJson(board))
            .bind("lobby_id", lobbyId.value)
            .executeAndReturnGeneratedKeys()
            .mapTo<JdbiIdModel>()
            .singleOrNull()?.toDomainModel()

    override fun deleteUserFromLobby(lobbyId: Id): Boolean =
        handle.createUpdate("delete from dbo.Lobbies where id = :lobbyId")
            .bind("lobbyId", lobbyId.value)
            .execute() == 1

    /**
     * Converts a board to a json string to be stored in the database, depending on the type of board.
     * @param board the board to be converted.
     * @return the json string representing the board.
     */
    private fun convertBoardToJdbiModelInJson(board: Board): String {
        val jdbiBoardModel = when (board) {
            is BoardWin -> JdbiBoardWinModel(grid = board.grid, winner = board.winner)
            is BoardDraw -> JdbiBoardDrawModel(grid = board.grid)
            is BoardRun -> JdbiBoardRunModel(
                grid = board.grid,
                turn = board.turn!!
            )
        }
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(jdbiBoardModel)
    }
}
