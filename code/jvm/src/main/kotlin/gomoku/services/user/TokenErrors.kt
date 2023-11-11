package gomoku.services.user

import gomoku.utils.Either
import kotlinx.datetime.Instant

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant
)

sealed class TokenCreationError {
    data object PasswordIsWrong : TokenCreationError()
    data object UsernameNotExists : TokenCreationError()
}

typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>

sealed class TokenRevocationError {
    data object TokenIsInvalid : TokenRevocationError()
}

typealias TokenRevocationResult = Either<TokenRevocationError, Boolean>
