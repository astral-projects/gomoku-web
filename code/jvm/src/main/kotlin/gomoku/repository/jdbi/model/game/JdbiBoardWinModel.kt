package gomoku.repository.jdbi.model.game

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.http.jackson.serializers.MovesSerializer
import gomoku.repository.jdbi.model.JdbiModel

class JdbiBoardWinModel (
    @field: JsonDeserialize(using = MovesDeserializer::class)
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves,
    val winner: Player,
    val boardSize: BoardSize
) : JdbiModel<BoardWin> {
    override fun toDomainModel(): BoardWin {
        return BoardWin(
            size = boardSize,
            mvs = grid,
            winner = winner
        )
    }
}