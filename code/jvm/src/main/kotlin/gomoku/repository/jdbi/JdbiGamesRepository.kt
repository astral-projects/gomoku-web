package gomoku.repository.jdbi

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.repository.GamesRepository
import gomoku.repository.jdbi.model.game.JdbiGameJoinVariantModel
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiGamesRepository(
    private val handle: Handle
) : GamesRepository {
    override fun getGameById(id: Id): Game? {
        // retrieve game information and corresponding variant
        val result = handle.createQuery(
            "select g.id, g.state, g.variant_id as variant_id, g.board, g.created_at, g.updated_at, g.host_id, g.guest_id, gv.name, gv.opening_rule, gv.board_size from dbo.Games as g join dbo.Gamevariants as gv on g.variant_id = gv.id where g.id = :id"
        )
            .bind("id", id.value)
            .mapTo<JdbiGameJoinVariantModel>()
            .singleOrNull() ?: return null
        return result.toDomainModel()
    }

    override fun startGame(variantId: Id, userId: Id): Boolean =
        handle.createUpdate(
            "insert into dbo.Lobbies (host_id, variant_id) " +
                    "values (:host_id, :variant_id)"
        )
            .bind("host_id", userId.value)
            .bind("variant_id", variantId.value)
            .execute()
            .run { this == 1 }

    override fun deleteGame(game: Game): Boolean {
        val r = handle.createUpdate("delete from dbo.Games where id = :gameId")
            .bind("gameId", game.id)
            .execute()
        return r == 1
    }

    override fun userBelongsToTheGame(user: User, gameId: Id): Boolean {
        val query =
            handle.createQuery("SELECT * FROM dbo.Games WHERE id = :gameId AND (host_id = :userId OR guest_id = :userId)")
                .bind("gameId", gameId.value)
                .bind("userId", user.id.value)
        val game = query.mapToMap().findOnly()
        return game != null // retorna true se encontrou um jogo, false caso contrário
    }


    override fun getSystemInfo(): SystemInfo {
        TODO("Not yet implemented")
    }

    override fun makeMove(gameId: Id, userId: Id, square: Square, player: Player): Boolean {
        val updateQuery = handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET board = jsonb_set(board, '{grid, -1}', :square::jsonb, true), 
            updated_at = extract(epoch from now()) 
        WHERE id = :gameId;
    """
        ).bind("gameId", gameId.value) // assumindo que gameId é um objeto e você quer usar um campo de valor
            .bind("square", "\"${square}-${player}\"") // assumindo que square tem um método toString adequado

        val rowsUpdated = updateQuery.execute()
        return rowsUpdated > 0 // retorna true se alguma linha foi atualizada, false caso contrário
    }

    override fun exitGame(id: Id, user: User): Boolean {
        val r = handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET state = 'FINISHED'
        WHERE id = :id AND (host_id = :userId OR guest_id = :userId)
    """
        )
            .bind("id", id)
            .bind("userId", user.id.value) // assuming that the object 'user' has an attribute 'id'
            .execute()

        return r == 1
    }

    override fun getGameStatus(gameId: Int, user: User): String? =
        handle.createQuery("select state from dbo.Games where id = :gameId AND (host_id = :id OR guest_id = :id)")
            .bind("id", user.id.value)
            .bind("gameId", gameId)
            .mapTo<String>()
            .one()
}

/*
   This will be helpful for the matchGame method
   handle.createUpdate("insert into dbo.Game (state, board_size, created, updated, game_variant, opening_rule, board) " +
               "values (:state, :board_size, :created, :updated, :game_variant, :opening_rule, CAST(:board AS jsonb))")
 */
