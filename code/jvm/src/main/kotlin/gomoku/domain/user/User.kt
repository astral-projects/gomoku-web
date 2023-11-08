package gomoku.domain.user

import gomoku.domain.components.Id
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username

data class User(
    val id: Id,
    val username: Username,
    val email: Email,
    val passwordValidation: PasswordValidationInfo
)
