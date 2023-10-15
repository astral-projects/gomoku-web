package gomoku.repository.jdbi

import gomoku.domain.game.Game
import gomoku.repository.GamesRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiGamesRepository(
    private val handle: Handle
) : GamesRepository {
    override fun getGameById(id: Int): Game? =
        handle.createQuery("select * from dbo.Game where game_id = :id")
            .bind("id", id)
            .mapTo<Game>()
            .singleOrNull()

    override fun createGame(gameVariant:String,openingRule: String,boardSize: Int, host:Int, guest:Int): Int? =
       handle.createUpdate("insert into dbo.Game (state, board_size, created, updated, game_variant, opening_rule, board) " +
               "values (:state, :board_size, :created, :updated, :game_variant, :opening_rule, CAST(:board AS jsonb))")
           .bind("state","IN_PROGRESS")
           .bind("game_variant", gameVariant)
           .bind("opening_rule", openingRule)
           .bind("board_size", boardSize)
           .bind("board", "[]")
           .bind("created", java.time.Instant.now().epochSecond.toInt())
           .bind("updated", java.time.Instant.now().epochSecond.toInt())
           .bind("host", host)
           .bind("guest", guest)
           .executeAndReturnGeneratedKeys()
           .mapTo<Int>()
           .one()

    override fun deleteGame(game: Game):Boolean {
        val r = handle.createUpdate("delete from dbo.Game where game_id = :gameId")
            .bind("gameId", game.game_id)
            .execute()
        return r == 1
    }
}