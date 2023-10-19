package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.http.jackson.serializers.MovesSerializer

class JdbiBoardRunModel(
    @field:JsonDeserialize(using = MovesDeserializer::class)
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves,
    val size: Int,
    val turn: BoardTurn
) : JdbiBoardModel {
    override fun toDomainModel(): BoardRun {
        return BoardRun(
            size = BoardSize.fromSize(size),
            mvs = grid,
            turn = turn,
            timeLeftInSec = turn.timeLeftInSec
        )
    }
}
