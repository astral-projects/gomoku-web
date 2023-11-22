package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.moves.Moves
import gomoku.repository.jackson.serializers.MovesDeserializer
import gomoku.repository.jackson.serializers.MovesSerializer

class JdbiBoardDrawModel(
    @field: JsonDeserialize(using = MovesDeserializer::class)
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves
) : JdbiBoardModel {
    override fun toDomainModel(): BoardDraw {
        return BoardDraw(moves = grid)
    }
}
