package gomoku.repository.jdbi.mappers

import gomoku.domain.game.GameId
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class GameIdMapper : ColumnMapper<GameId> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): GameId = GameId(r.getInt(columnNumber))
}
