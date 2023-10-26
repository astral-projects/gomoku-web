package gomoku.domain.components

import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.user.User
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
    object BlankUsername : UsernameError()
}
typealias GettingUsernameResult = Either<UsernameError, Username>

sealed class UserError {
    object UserIsBlank : UserError()
    object InvalidLength : UserError()
}
typealias GettingUserResult = Either<UserError, User>

sealed class PasswordError {
    object PasswordNotSafe : PasswordError()
    object PasswordIsEmptyOrBlanck : PasswordError()
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

sealed class SquareError {
    class ColumnErrorType(val error: ColumnError) : SquareError()
    class RowErrorType(val error: RowError) : SquareError()
}
typealias GettingSquareResult = Either<SquareError, Square>
