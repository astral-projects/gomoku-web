package gomoku.services.user

import gomoku.domain.Id
import gomoku.domain.user.User
import gomoku.utils.Either

sealed class UserCreationError {
    object UsernameAlreadyExists : UserCreationError()
    object EmailAlreadyExists : UserCreationError()
    object InsecurePassword : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Id>

sealed class GettingUserError {
    object UserNotFound : GettingUserError()
}

typealias GettingUserResult = Either<GettingUserError, User>
