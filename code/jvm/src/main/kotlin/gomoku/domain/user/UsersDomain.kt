package gomoku.domain.user

import gomoku.domain.token.Token
import gomoku.domain.token.TokenEncoder
import gomoku.domain.token.TokenValidationInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig
) {

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes.value).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean = try {
        Base64.getUrlDecoder()
            .decode(token).size == config.tokenSizeInBytes.value
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun validatePassword(password: String, validationInfo: PasswordValidationInfo) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo
    )

    fun createPasswordValidationInformation(password: String) = PasswordValidationInfo(
        validationInfo = passwordEncoder.encode(password)
    )

    fun isTokenTimeValid(
        clock: Clock,
        token: Token
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now &&
            (now - token.createdAt) <= config.tokenTtl &&
            (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String): TokenValidationInfo =
        tokenEncoder.createValidationInformation(token)

    val maxNumberOfTokensPerUser = config.maxTokensPerUser
}
