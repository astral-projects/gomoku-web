package gomoku.http.jackson

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedField
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.util.*

@Configuration
class JacksonConfig {

    companion object {
        private val logger = LoggerFactory.getLogger(JacksonConfig::class.java)
    }

    // TODO("not working... why?")
    @Bean
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        logger.info("Configuring JackSonObjectMapperBuilder with custom property naming strategy")
        return Jackson2ObjectMapperBuilder()
            .propertyNamingStrategy(object : PropertyNamingStrategy() {
                override fun nameForField(config: MapperConfig<*>, field: AnnotatedField, defaultName: String): String {
                    return super.nameForField(config, field, defaultName).lowercase(Locale.getDefault())
                }
            })
    }
}
