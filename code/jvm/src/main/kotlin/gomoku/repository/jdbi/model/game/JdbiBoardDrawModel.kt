package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.http.jackson.serializers.MovesSerializer

class JdbiBoardDrawModel(
    @field: JsonDeserialize(using = MovesDeserializer::class)
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves,
    val size: Int
) : JdbiBoardModel {
    override fun toDomainModel(): BoardDraw {
        return BoardDraw(
            size = BoardSize.fromSize(size),
            mvs = grid
        )
    }
}
