package gomoku.repository.jdbi.mappers

import gomoku.domain.game.GameState
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class GameStateMapper : ColumnMapper<GameState> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): GameState =
        GameState.valueOf(r.getString(columnNumber))
}
