package gomoku.http.jackson.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import gomoku.domain.game.board.BoardSize
import java.io.IOException

class BoardSizeSerializer : JsonSerializer<BoardSize>() {
    @Throws(IOException::class)
    override fun serialize(value: BoardSize, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeNumber(value.size)
    }
}
