package gomoku.repository.jdbi.mappers

import gomoku.domain.Id
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class GameIdMapper : ColumnMapper<Id> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): Id = Id(r.getInt(columnNumber))
}
