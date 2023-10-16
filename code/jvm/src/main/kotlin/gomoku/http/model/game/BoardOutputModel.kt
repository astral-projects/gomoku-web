package gomoku.http.model.game

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.moves.Moves
import gomoku.http.jackson.serializers.MovesSerializer
import gomoku.http.model.JsonOutputModel

class BoardOutputModel private constructor(
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves,
    val turn: BoardTurn
) {
    companion object : JsonOutputModel<Board, BoardOutputModel> {
        override fun serializeFrom(domainClass: Board): BoardOutputModel =
            BoardOutputModel(
                grid = domainClass.grid,
                turn = domainClass.turn
            )
    }
}