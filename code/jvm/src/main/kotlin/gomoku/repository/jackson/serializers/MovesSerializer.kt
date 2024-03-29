package gomoku.repository.jackson.serializers

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
import gomoku.utils.get
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

private const val MOVE_SEPARATOR = "-"

class MovesSerializer : JsonSerializer<Moves>() {

    @Throws(IOException::class)
    override fun serialize(value: Moves, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartArray()
        value.forEach { (square, piece) ->
            val pieceString = piece.player.name.lowercase(Locale.getDefault())
            // Example: a1-w or a11-b
            val moveString = "$square$MOVE_SEPARATOR$pieceString"
            gen.writeString(moveString)
        }
        gen.writeEndArray()
    }
}

class MovesDeserializer : JsonDeserializer<Moves>() {
    @Throws(IOException::class, IllegalArgumentException::class)
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): Moves {
        val node = jsonParser.codec.readTree<JsonNode>(jsonParser)
        try {
            return node.fold(emptyMap()) { moves, move ->
                val (squareString, playerString) = move.asText().split(MOVE_SEPARATOR)
                val square = when (squareString.length) {
                    // Example: a1 from a1-w
                    2 -> Square(
                        Column(value = squareString.first()).get(),
                        Row(index = squareString.last().toString().toInt() - 1).get()
                    )
                    // Example: a11 from a11-b
                    3 -> Square(
                        Column(value = squareString.first()).get(),
                        Row(squareString.drop(1).toInt() - 1).get()
                    )

                    else -> throw IllegalArgumentException("Invalid square: $squareString")
                }
                val player = Player.valueOf(playerString.first().uppercase(Locale.getDefault()))
                moves + (square to Piece(player))
            }
        } catch (ex: IllegalArgumentException) {
            logger.info("Invalid move: ${ex.message}")
            throw ex
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MovesDeserializer::class.java)
    }
}
