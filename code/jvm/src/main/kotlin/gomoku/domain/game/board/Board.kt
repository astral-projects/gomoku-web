package gomoku.domain.game.board

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.http.jackson.serializers.MovesDeserializer
import gomoku.http.jackson.serializers.MovesSerializer

data class Board(
    @field:JsonSerialize(using = MovesSerializer::class)
    @field:JsonDeserialize(using = MovesDeserializer::class)
    val grid: Moves,
    val turn: BoardTurn
) {

    // No-argument constructor for Jackson
    constructor() : this(emptyMap<Square, Piece>(), BoardTurn(Player.w, 0))

}