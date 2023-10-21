package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import gomoku.domain.game.board.Board
import gomoku.repository.jdbi.model.JdbiModel

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JdbiBoardRunModel::class, name = "run"),
    JsonSubTypes.Type(value = JdbiBoardWinModel::class, name = "win"),
    JsonSubTypes.Type(value = JdbiBoardDrawModel::class, name = "draw")
)
sealed interface JdbiBoardModel : JdbiModel<Board> {
    override fun toDomainModel(): Board {
        return when (this) {
            is JdbiBoardRunModel -> this.toDomainModel()
            is JdbiBoardWinModel -> this.toDomainModel()
            is JdbiBoardDrawModel -> this.toDomainModel()
        }
    }
}
