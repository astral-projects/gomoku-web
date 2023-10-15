package gomoku.repository.jdbi.mappers

import gomoku.domain.game.GameVariant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class GameVariantMapper : ColumnMapper<GameVariant> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): GameVariant =
        GameVariant.valueOf(r.getString(columnNumber))
}
