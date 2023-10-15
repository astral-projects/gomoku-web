package gomoku.http.jackson.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import java.io.IOException

private const val MOVE_SPLITTER = "-"

class MovesSerializer : JsonSerializer<Moves>() {
    @Throws(IOException::class)
    override fun serialize(value: Moves, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartArray()
        value.forEach { (square, piece) ->
            val squareString = "${square.col.letter}${square.row.number}"
            val pieceString = piece.player.name
            val moveString = "$squareString$MOVE_SPLITTER$pieceString"
            gen.writeString(moveString)
        }
        gen.writeEndArray()
    }
}

class MovesDeserializer : JsonDeserializer<Moves>() {
    @Throws(IOException::class)
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): Moves {
        val node = jsonParser.codec.readTree<JsonNode>(jsonParser)
        return node.fold(emptyMap()) { moves, move ->
            val (squareString, playerString) = move.asText().split(MOVE_SPLITTER)
            val square = if (squareString.length == 2) {
                Square(Column(squareString.first()), Row(squareString.last().toString().toInt()))
            } else {
                println(squareString)
                Square(Column(squareString.first()), Row(squareString.drop(1).toInt()))
            }
            val player = Player.valueOf(playerString)
            moves + (square to Piece(player))
        }
    }
}
