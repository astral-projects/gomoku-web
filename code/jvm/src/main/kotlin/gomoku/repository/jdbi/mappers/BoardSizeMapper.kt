package gomoku.repository.jdbi.mappers

import gomoku.domain.game.board.BoardSize
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class BoardSizeMapper : ColumnMapper<BoardSize> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): BoardSize {
        val size = r.getInt(columnNumber)
        return BoardSize.values().find { it.size == size } ?: throw SQLException("Invalid BoardSize value in database")
    }
}
