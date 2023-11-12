package gomoku

import gomoku.domain.components.PositiveValue
import gomoku.domain.token.Sha256TokenEncoder
import gomoku.domain.user.UsersDomainConfig
import gomoku.repository.jdbi.configureWithAppRequirements
import gomoku.utils.RequiresDatabaseConnection
import gomoku.utils.get
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class GomokuApplication {
    @Bean
    fun jdbi() = Jdbi.create(
        PGSimpleDataSource().apply {
            setURL(Environment.getDbUrl())
        }
    ).configureWithAppRequirements()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock() = Clock.System

    @Bean
    fun usersDomainConfig() = UsersDomainConfig(
        tokenSizeInBytes = PositiveValue(256 / 8).get(),
        tokenTtl = 24.hours,
        tokenRollingTtl = 1.hours,
        maxTokensPerUser = PositiveValue(3).get()
    )
}

@RequiresDatabaseConnection
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("GomokuApplication")
    logger.info("Starting application")
    logger.info("DB_URL: ${Environment.getDbUrl()}")
    // used for spring logger messages
    Locale.setDefault(Locale.ENGLISH)
    runApplication<GomokuApplication>(*args)
}
