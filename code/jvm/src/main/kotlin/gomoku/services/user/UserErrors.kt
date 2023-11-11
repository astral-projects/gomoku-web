package gomoku.services.user

import gomoku.domain.components.Id
import gomoku.domain.user.User
import gomoku.utils.Either

sealed class UserCreationError {
    data object UsernameAlreadyExists : UserCreationError()
    data object EmailAlreadyExists : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Id>

sealed class GettingUserError {
    data object UserNotFound : GettingUserError()
}

typealias GettingUserResult = Either<GettingUserError, User>
