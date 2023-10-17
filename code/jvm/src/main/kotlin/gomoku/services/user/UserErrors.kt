package gomoku.services.user

import gomoku.repository.jdbi.model.user.JdbiUserModel
import gomoku.utils.Either

sealed class UserCreationError {
    object UserAlreadyExists : UserCreationError()
    object InsecurePassword : UserCreationError()
}

typealias UserCreationResult = Either<UserCreationError, Int>

sealed class GettingUserError {
    object UserNotFound : GettingUserError()
}

typealias GettingUserResult = Either<GettingUserError, JdbiUserModel>