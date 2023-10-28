package gomoku.domain.components

import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Password
import gomoku.domain.user.components.Username
import gomoku.utils.Either

sealed class IdError(open val value: Int) {
    data class InvalidIdError(override val value: Int) : IdError(value)
}
typealias GettingIdResult = Either<IdError, Id>

sealed class InvalidEmailError {
    object InvalidEmail : InvalidEmailError()
}
typealias GettingEmailResult = Either<InvalidEmailError, Email>

sealed class NonNegativeValueError {
    object InvalidNonNegativeValue : NonNegativeValueError()
}
typealias NonNegativeValueResult = Either<NonNegativeValueError, NonNegativeValue>

sealed class PositiveValueError {
    data class InvalidPositiveValue(val value: Int) : PositiveValueError()
}
typealias PositiveValueResult = Either<PositiveValueError, PositiveValue>

sealed class UsernameError {
    object InvalidLength : UsernameError()
    object EmptyUsername : UsernameError()
    object BlankUsername : UsernameError()
}
typealias GettingUsernameResult = Either<UsernameError, Username>

sealed class PasswordError {
    object PasswordNotSafe : PasswordError()
    object PasswordBlank : PasswordError()
    object PasswordIsEmpty : PasswordError()
}
typealias GettingPasswordResult = Either<PasswordError, Password>

sealed class RowError {
    data class InvalidRow(val value: Int) : RowError()
}
typealias GettingRowResult = Either<RowError, Row>

sealed class ColumnError {
    data class InvalidColumn(val value: Char) : ColumnError()
}
typealias GettingColumnResult = Either<ColumnError, Column>
