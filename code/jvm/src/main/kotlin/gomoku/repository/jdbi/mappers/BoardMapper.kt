package gomoku.repository.jdbi.mappers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gomoku.domain.game.board.Board
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class BoardMapper : ColumnMapper<Board> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): Board {
        val objectMapper = jacksonObjectMapper()
        val json = r.getString(columnNumber)
        return objectMapper.readValue(json, Board::class.java)
    }
}
