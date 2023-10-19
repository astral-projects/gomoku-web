package gomoku.repository.jdbi.mappers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gomoku.repository.jdbi.model.game.JdbiBoardModel
import gomoku.repository.jdbi.model.game.JdbiBoardRunModel
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException

class JdbiBoardModelMapper : ColumnMapper<JdbiBoardModel> {
    @Throws(SQLException::class)
    override fun map(r: ResultSet, columnNumber: Int, ctx: StatementContext?): JdbiBoardModel {
        val objectMapper = jacksonObjectMapper()
        val json = r.getString(columnNumber)
        return objectMapper.readValue(json, JdbiBoardModel::class.java)
    }
}
