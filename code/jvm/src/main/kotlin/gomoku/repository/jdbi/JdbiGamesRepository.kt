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

    override fun createGame(game: Game): Game {
        TODO("Not yet implemented")
    }

    override fun deleteGame(game: Game) {
        TODO("Not yet implemented")
    }
}