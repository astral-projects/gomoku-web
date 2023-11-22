package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Moves
import gomoku.repository.jackson.serializers.MovesDeserializer
import gomoku.repository.jackson.serializers.MovesSerializer

class JdbiBoardWinModel(
    @field: JsonDeserialize(using = MovesDeserializer::class)
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves,
    val winner: Player
) : JdbiBoardModel {
    override fun toDomainModel(): BoardWin {
        return BoardWin(
            moves = grid,
            winner = winner
        )
    }
}
