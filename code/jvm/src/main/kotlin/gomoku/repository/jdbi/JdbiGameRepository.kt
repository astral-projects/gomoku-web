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

/**
 * Implementation of the GamesRepository interface
 * using JDBI for database interaction.
 */
class JdbiGameRepository(
    private val handle: Handle
) : GamesRepository {
    /**
     * Retrieves a game by its id.
     *
     * @param id the id of the game to retrieve
     * @return the game with the given id, or null if no such game exists
     */
    override fun getGameById(id: Id): Game? =
        handle.createQuery(
            "select g.id, g.state, g.variant_id as variant_id, g.board, g.created_at, g.updated_at, g.host_id, g.guest_id, gv.name, gv.opening_rule, gv.board_size from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = :id"
        )
            .bind("id", id.value)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    /**
     * Inserts a list of variants into the database.
     *
     * @param variants the list of variants to insert
     * @return true if the variants were inserted successfully, false otherwise
     */
    override fun insertVariants(variants: List<VariantConfig>): Boolean {
        val values = mutableListOf<String>()
        for (variant in variants) {
            values.add("('${variant.name}', '${variant.openingRule}', ${variant.boardSize.size})")
        }
        val valuesString = values.joinToString(", ")
        return handle.createUpdate("insert into dbo.gamevariants (name, opening_rule, board_size) values $valuesString on conflict (name) do nothing")
            .execute() == 1
    }

    /**
     * Retrieves a variant by its name.
     *
     * @param variantName the name of the variant to retrieve
     * @return the id of the variant with the given name, or null if no such variant exists
     */
    override fun getVariantByName(variantName: VariantName): Id {
        return handle.createQuery("select id from dbo.GameVariants where name = :variantName")
            .bind("variantName", variantName)
            .mapTo<JdbiIdModel>()
            .single().toDomainModel()
    }

    /**
     * Retrieves all variants from the database.
     *
     * @return a list of all variants
     */
    override fun getVariants(): List<GameVariant> =
        handle.createQuery("select * from dbo.Gamevariants")
            .mapTo<JdbiVariantModel>()
            .map { it.toDomainModel() }
            .toList()

    /**
     * Adds a user to a lobby.
     *
     * @param variantId the id of the variant of the lobby
     * @param userId the id of the user to add to the lobby
     * @return the id of the lobby the user was added to, or null if the user could not be added
     */
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

    /**
     * Checks if a user is in a lobby.
     *
     * @param userId the id of the user to check
     * @return the lobby the user is in, or null if the user is not in a lobby
     */
    override fun checkIfUserIsInLobby(userId: Id): Lobby? =
        handle.createQuery("select * from dbo.Lobbies where host_id = :userId")
            .bind("userId", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull()?.toDomainModel()

    /**
     * Deletes a game from the database. This method is only used for testing.
     *
     * @param gameId the id of the game to delete
     * @param userId the id of the user who is deleting the game
     * @return true if the game was deleted successfully, false otherwise
     */
    override fun deleteGame(gameId: Id, userId: Id): Boolean =
        handle.createUpdate("delete from dbo.Games where id = :gameId and host_id = :hostId and state = :state")
            .bind("gameId", gameId.value)
            .bind("hostId", userId.value)
            .bind("state", GameState.FINISHED.name)
            .execute() == 1

    /**
     * Checks if a user is in a game.
     *
     * @param userId the id of the user to check
     * @return the game the user is in, or null if the user is not in a game
     */
    override fun findIfUserIsInGame(userId: Id): Game? =
        handle.createQuery(
            """SELECT g.*, gv.name, gv.opening_rule, gv.board_size FROM dbo.Games AS g JOIN dbo.GameVariants AS gv ON g.variant_id = gv.id  WHERE
                g.state != :state AND (g.host_id = :userId OR g.guest_id = :userId);"""
        )
            .bind("userId", userId.value)
            .bind("state", GameState.FINISHED.name)
            .mapTo<JdbiGameAndVariantModel>()
            .singleOrNull()?.toDomainModel()

    /**
     * Checks if a user belongs to a game.
     *
     * @param userId the id of the user to check
     * @param gameId the id of the game to check
     * @return the game the user belongs to, or null if the user does not belong to the game
     */
    override fun userBelongsToTheGame(userId: Id, gameId: Id): Game?=
            handle.createQuery("SELECT g.*, gv.name, gv.opening_rule, gv.board_size FROM dbo.Games AS g " +
                    "JOIN dbo.GameVariants AS gv ON g.variant_id = gv.id WHERE g.id = :gameId AND (g.host_id = :userId OR g.guest_id = :userId)")
                .bind("gameId", gameId.value)
                .bind("userId", userId.value)
                .mapTo<JdbiGameAndVariantModel>()
                .singleOrNull()?.toDomainModel()

    /**
     * Checks if a user is the host of a game.
     *
     * @param userId the id of the user to check
     * @param gameId the id of the game to check
     * @return the game the user is the host of, or null if the user is not the host of the game
     */
    override fun userIsTheHost(userId: Id, gameId: Id): Game? =
            handle.createQuery("SELECT g.*, gv.name, gv.opening_rule, gv.board_size FROM dbo.Games AS g " +
                    "JOIN dbo.GameVariants AS gv ON g.variant_id = gv.id WHERE g.id = :gameId AND g.host_id = :userId")
                .bind("gameId", gameId.value)
                .bind("userId", userId.value)
                .mapTo<JdbiGameAndVariantModel>()
                .singleOrNull()?.toDomainModel()

    /**
     * Updates the points of the players in a game.
     *
     * @param gameId the id of the game to update
     * @param winnerId the id of the winner of the game
     * @param loserId the id of the loser of the game
     * @param winnerPoints the points to add to the winner
     * @param loserPoints the points to add to the loser
     * @param shouldCountAsGameWin whether the game should count as a win for the winner
     * @return true if the points were updated successfully, false otherwise
     */
    override fun updatePoints(
        gameId: Id,
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
                END
        WHERE user_id IN (:winnerId, :loserId)
        """
        )
            .bind("winnerId", winnerId.value)
            .bind("loserId", loserId.value)
            .bind("winnerPoints", winnerPoints.value)
            .bind("loserPoints", loserPoints.value)
            .bind("winPoint", if (shouldCountAsGameWin) 1 else 0)
            .execute() > 0

    /**
     * Deletes a variant from the database. This method is only used for testing.
     *
     * @param name the name of the variant to delete
     * @return true if the variant was deleted successfully, false otherwise
     */
    override fun deleteVariant(name: VariantName): Boolean {
        return handle.createUpdate("delete from dbo.Gamevariants where name = :name")
            .bind("name", name)
            .execute() == 1
    }

    /**
     * Deletes a lobby from the database. This method is only used for testing.
     *
     * @param lobbyId the id of the lobby to delete
     * @return true if the lobby was deleted successfully, false otherwise
     */
    override fun deleteLobby(lobbyId: Id): Boolean {
        return handle.createUpdate("delete from dbo.Lobbies where id = :lobbyId")
            .bind("lobbyId", lobbyId.value)
            .execute() == 1
    }

    /**
     * Updates a game in the database.
     *
     * @param gameId the id of the game to update
     * @param board the board to update the game with
     * @param gameState the state to update the game with
     * @return true if the game was updated successfully, false otherwise
     */
    override fun updateGame(gameId: Id, board: Board, gameState: GameState): Boolean =
        handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET board = :board::jsonb, updated_at = extract(epoch from now()), state = :state 
        WHERE id = :gameId;
    """
        )
            .bind("gameId", gameId.value)
            .bind("state",gameState.name)
            .bind("board", convertBoardToJdbiModelInJson(board)).execute() > 0

    /**
     * Exits a game.
     *
     * @param gameId the id of the game to exit
     * @param userId the id of the user who is exiting the game
     * @return the id of the user who stayed in the game, or null if the user could not exit the game
     */
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

    /**
     * Checks if a user is in matchmaking.
     * This means that it already exists a lobby with the given variant id and the user is not the host of the lobby.
     *
     * @param variantId the id of the variant to check
     * @param userId the id of the user to check
     * @return the lobby the user is in matchmaking, or null if the user is not in matchmaking
     */
    override fun isMatchmaking(variantId: Id, userId: Id): Lobby? =
        handle.createQuery("select * from dbo.Lobbies where variant_id = :variant_id and host_id != :host_id FOR UPDATE")
            .bind("variant_id", variantId.value)
            .bind("host_id", userId.value)
            .mapTo<JdbiLobbyModel>()
            .singleOrNull()?.toDomainModel()

    /**
     * Creates a game in the database.
     *
     * @param variantId the id of the variant of the game
     * @param hostId the id of the host of the game
     * @param guestId the id of the guest of the game
     * @param lobbyId the id of the lobby that the guests joined
     * @param board the board of the game
     */
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

    /**
     * Deletes a user form a lobby.
     *
     * @param lobbyId the id of the lobby to delete the user from
     * @return true if the user was deleted successfully, false otherwise
     */
    override fun deleteUserFromLobby(lobbyId: Id): Boolean =
        handle.createUpdate("delete from dbo.Lobbies where id = :lobbyId")
            .bind("lobbyId", lobbyId.value)
            .execute() == 1

    /**
     * Converts a board to a JDBI model in JSON format.
     *
     * @param board the board to convert
     * @return the board in JSON format
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

