package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.repository.jdbi.model.JdbiModel

class JdbiBoardModel(
    @field:JsonDeserialize(using = MovesDeserializer::class)
    val grid: Moves,
    val turn: BoardTurn
) : JdbiModel<Board> {
    override fun toDomainModel(): Board {
        return Board(
            grid = grid,
            turn = turn
        )
    }
}
