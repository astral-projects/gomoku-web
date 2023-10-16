package gomoku.repository.jdbi

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.domain.user.UserId
import gomoku.repository.GamesRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiGamesRepository(
    private val handle: Handle
) : GamesRepository {
    override fun getGameById(id: Int): Game? =
        handle.createQuery("select * from dbo.Games where id = :id")
            .bind("id", id)
            .mapTo<Game>()
            .singleOrNull()

    override fun startGame(gameVariant: String, openingRule: String, boardSize: Int, user: Int): Int? =
        handle.createUpdate(
            "insert into dbo.Lobbies ( board_size, game_variant, opening_rule, host_id) " +
                    "values (:board_size, :game_variant, :opening_rule, :host_id)"
        )
            .bind("game_variant", gameVariant)
            .bind("opening_rule", openingRule)
            .bind("board_size", boardSize)
            .bind("host_id", user)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun deleteGame(game: Game): Boolean {
        val r = handle.createUpdate("delete from dbo.Games where game_id = :gameId")
            .bind("gameId", game.id)
            .execute()
        return r == 1
    }

    override fun userBelongsToTheGame(user: User, gameId: GameId): Boolean {
        val query =
            handle.createQuery("SELECT * FROM dbo.Games WHERE id = :gameId AND (host_id = :userId OR guest_id = :userId)")
                .bind("gameId", gameId.id)
                .bind("userId", user.id.value)
        val game = query.mapToMap().findOnly()
        return game != null // retorna true se encontrou um jogo, false caso contrário
    }


    override fun getSystemInfo(): SystemInfo {
        TODO("Not yet implemented")
    }

    override fun makeMove(gameId: GameId, userId: User, square: Square, player: Player): Boolean {
        val updateQuery = handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET board = jsonb_set(board, '{grid, -1}', :square::jsonb, true), 
            updated_at = extract(epoch from now()) 
        WHERE id = :gameId;
    """
        ).bind("gameId", gameId.id) // assumindo que gameId é um objeto e você quer usar um campo de valor
            .bind("square", "\"${square}-${player}\"") // assumindo que square tem um método toString adequado

        val rowsUpdated = updateQuery.execute()
        return rowsUpdated > 0 // retorna true se alguma linha foi atualizada, false caso contrário
    }

    override fun exitGame(gameId: Int, user: User): Boolean {
        val r = handle.createUpdate(
            """
        UPDATE dbo.Games 
        SET state = 'FINISHED'
        WHERE id = :gameId AND (host_id = :userId OR guest_id = :userId)
    """
        )
            .bind("gameId", gameId)
            .bind("userId", user.id.value) // assumindo que o objeto 'user' tem um atributo 'id'
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