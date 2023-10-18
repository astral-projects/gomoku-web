package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.boardSize
import gomoku.domain.game.board.isFinished
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.repository.jdbi.model.JdbiModel

class JdbiBoardModel(
    @field:JsonDeserialize(using = MovesDeserializer::class)
    val grid: Moves,
    val turn: BoardTurn
) : JdbiModel<Board> {
    override fun toDomainModel(): Board {
        return BoardRun(
            size = boardSize,
            mvs = grid,
            turn = turn,
            timeLeftInSec = turn.timeLeftInSec,
        )
    }
}
