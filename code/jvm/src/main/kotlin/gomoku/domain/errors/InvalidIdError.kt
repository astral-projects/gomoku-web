package gomoku.domain.errors

import gomoku.domain.Id
import gomoku.domain.user.Email
import gomoku.utils.Either

sealed class InvalidIdError {
    object InvalidId : InvalidIdError()
}

typealias GettingIdResult = Either<InvalidIdError, Id>

sealed class InvalidEmailError {
    object InvalidEmail : InvalidEmailError()
}

typealias GettingEmailResult = Either<InvalidEmailError, Email>