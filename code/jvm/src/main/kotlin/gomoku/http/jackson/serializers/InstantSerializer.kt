package gomoku.http.jackson.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import kotlinx.datetime.Instant
import java.io.IOException

class InstantSerializer : JsonSerializer<Instant>() {
    @Throws(IOException::class)
    override fun serialize(
        instant: Instant,
        jgen: JsonGenerator,
        provider: SerializerProvider
    ) {
        jgen.writeString(instant.toString())
    }
}
