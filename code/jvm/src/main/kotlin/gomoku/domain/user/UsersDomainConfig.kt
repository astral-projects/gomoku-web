package gomoku.domain.user

import gomoku.domain.components.PositiveValue
import kotlin.time.Duration

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
