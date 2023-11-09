package gomoku.domain.user

import gomoku.domain.components.PositiveValue
import kotlin.time.Duration

/**
 * Represents the configuration of the users domain.
 * @property tokenSizeInBytes The size of the token in bytes.
 * @property tokenTtl The time to live of the token.
 * @property tokenRollingTtl The time to live of the token after it has been used.
 * @property maxTokensPerUser The maximum number of tokens per user.
 */
data class UsersDomainConfig(
    val tokenSizeInBytes: PositiveValue,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerUser: PositiveValue
) {
    init {
        require(tokenTtl.isPositive())
        require(tokenRollingTtl.isPositive())
    }
}
