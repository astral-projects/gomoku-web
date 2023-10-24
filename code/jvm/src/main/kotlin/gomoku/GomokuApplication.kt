package gomoku

import gomoku.domain.PositiveValue
import gomoku.domain.token.Sha256TokenEncoder
import gomoku.domain.user.UsersDomainConfig
import gomoku.http.pipeline.interceptors.AuthenticationInterceptor
import gomoku.http.pipeline.resolvers.AuthenticatedUserArgumentResolver
import gomoku.repository.jdbi.configureWithAppRequirements
import gomoku.utils.get
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

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("GomokuApplication")
    logger.info("Starting application")
    logger.info("DB_URL: ${Environment.getDbUrl()}")
    // used for spring logger messages
    Locale.setDefault(Locale.ENGLISH)
    runApplication<GomokuApplication>(*args)
}
