package gomoku.http.jackson.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.IOException

class InstantSerializer : JsonSerializer<Instant>() {
    @Throws(IOException::class)
    override fun serialize(
        instant: Instant,
        jgen: JsonGenerator,
        provider: SerializerProvider
    ) {
        val formattedTimestamp = formatTimestamp(instant)
        jgen.writeString(formattedTimestamp)
    }

    private fun formatTimestamp(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
        val year = localDateTime.year
        val month = localDateTime.monthNumber
        val day = localDateTime.dayOfMonth
        val hour = localDateTime.hour
        val minute = localDateTime.minute
        val second = localDateTime.second
        return "$year-$month-$day $hour:$minute:$second"
    }
}
