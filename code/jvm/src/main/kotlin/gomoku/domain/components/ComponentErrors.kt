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

sealed class EmailError {
    data object InvalidEmail : EmailError()
}
typealias GettingEmailResult = Either<EmailError, Email>

sealed class NonNegativeValueError {
    data object InvalidNonNegativeValue : NonNegativeValueError()
}
typealias NonNegativeValueResult = Either<NonNegativeValueError, NonNegativeValue>

sealed class PositiveValueError {
    data class InvalidPositiveValue(val value: Int) : PositiveValueError()
}
typealias PositiveValueResult = Either<PositiveValueError, PositiveValue>

sealed class UsernameError {
    data object InvalidLength : UsernameError()
    data object UsernameBlank : UsernameError()
}
typealias GettingUsernameResult = Either<UsernameError, Username>

sealed class PasswordError {
    data object PasswordNotSafe : PasswordError()
    data object PasswordBlank : PasswordError()
}
typealias GettingPasswordResult = Either<PasswordError, Password>

sealed class RowError {
    data class InvalidRow(val value: Any) : RowError()
}
typealias GettingRowResult = Either<RowError, Row>

sealed class ColumnError {
    data class InvalidColumn(val value: Any) : ColumnError()
}
typealias GettingColumnResult = Either<ColumnError, Column>

sealed class TermError {
    data object InvalidLength : TermError()
}
typealias GettingTermResult = Either<TermError, Term>
