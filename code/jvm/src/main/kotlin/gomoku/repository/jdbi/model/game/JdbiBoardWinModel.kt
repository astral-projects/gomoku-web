package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.http.jackson.serializers.MovesSerializer

class JdbiBoardWinModel (
    @field: JsonDeserialize(using = MovesDeserializer::class)
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves,
    val winner: Player,
    val size: Int
) : JdbiBoardModel {
    override fun toDomainModel(): BoardWin {
        return BoardWin(
            size = BoardSize.fromSize(size),
            mvs = grid,
            winner = winner
        )
    }
}