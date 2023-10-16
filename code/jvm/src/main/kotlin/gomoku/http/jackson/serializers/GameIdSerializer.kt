package gomoku.http.jackson.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import gomoku.domain.Id
import java.io.IOException

class GameIdSerializer : JsonSerializer<Id>() {
    @Throws(IOException::class)
    override fun serialize(value: Id, jgen: JsonGenerator, provider: SerializerProvider) {
        jgen.writeNumber(value.value)
    }
}
