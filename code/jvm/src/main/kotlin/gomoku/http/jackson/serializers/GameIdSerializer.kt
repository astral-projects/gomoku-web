package gomoku.http.jackson.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import gomoku.domain.game.GameId
import java.io.IOException

class GameIdSerializer : JsonSerializer<GameId>() {
    @Throws(IOException::class)
    override fun serialize(value: GameId, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeNumber(value.id)
    }
}
