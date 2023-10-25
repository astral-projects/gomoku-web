package gomoku.domain.errors

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.user.Email
import gomoku.domain.user.Password
import gomoku.domain.user.User
import gomoku.domain.user.Username
import gomoku.utils.Either

sealed class InvalidIdError(open val value: Int) {
    data class InvalidId(override val value: Int) : InvalidIdError(value)
}

typealias GettingIdResult = Either<InvalidIdError, Id>

sealed class InvalidEmailError {
    object InvalidEmail : InvalidEmailError()
}
typealias GettingEmailResult = Either<InvalidEmailError, Email>

sealed class NonNegativeValueError {
    object InvalidNonNegativeValue : NonNegativeValueError()
}
typealias NonNegativeValueResult = Either<NonNegativeValueError, NonNegativeValue>

sealed class InvalidPositiveValueError {
    data class InvalidPositiveValue(val value: Int) : InvalidPositiveValueError()
}
typealias PositiveValueResult = Either<InvalidPositiveValueError, PositiveValue>

sealed class InvalidUsernameError {
    object InvalidLength : InvalidUsernameError()
    object BlankUsername : InvalidUsernameError()
}

typealias GettingUsernameResult = Either<InvalidUsernameError, Username>

sealed class InvalidUserError {
    object UserIsBlank : InvalidUserError()
    object InvalidLength : InvalidUserError()
}

typealias GettingUserResult = Either<InvalidUserError, User>

sealed class InvalidPasswordError {
    object PasswordNotSafe : InvalidPasswordError()
    object PasswordIsEmpty : InvalidPasswordError()
}

typealias GettingPasswordResult = Either<InvalidPasswordError, Password>
