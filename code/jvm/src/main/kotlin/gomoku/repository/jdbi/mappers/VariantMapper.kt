package gomoku.repository.jdbi.mappers

import gomoku.domain.game.Variant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class VariantMapper : ColumnMapper<Variant> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): Variant =
        Variant.valueOf(r.getString(columnNumber))
}
