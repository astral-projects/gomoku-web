package gomoku.services.user

import gomoku.utils.Either
import kotlinx.datetime.Instant

data class TokenExternalInfo(
    val tokenValue: String,
    val tokenExpiration: Instant
)

sealed class TokenCreationError {
    object PasswordIsWrong : TokenCreationError()
    object UsernameNotExists : TokenCreationError()
}

typealias TokenCreationResult = Either<TokenCreationError, TokenExternalInfo>

sealed class TokenRevocationError {
    object TokenIsInvalid : TokenRevocationError()
}

typealias TokenRevocationResult = Either<TokenRevocationError, Boolean>
