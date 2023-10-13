package gomoku

import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import gomoku.domain.Sha256TokenEncoder
import gomoku.domain.UsersDomainConfig
import gomoku.http.pipeline.AuthenticatedUserArgumentResolver
import gomoku.http.pipeline.AuthenticationInterceptor
import gomoku.repository.jdbi.configureWithAppRequirements
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class TicTacToeApplication {
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
        tokenSizeInBytes = 256 / 8,
        tokenTtl = 24.hours,
        tokenRollingTtl = 1.hours,
        maxTokensPerUser = 3
    )
}

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("TicTacToeApplication")
    logger.info("Starting application")
    logger.info("DB_URL: ${Environment.getDbUrl()}")
    runApplication<TicTacToeApplication>(*args)
}
