package gomoku.domain.user

import gomoku.domain.components.Id
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username

/**
 * Represents a user.
 * @property id The Id of the user.
 * @property username The username of the user.
 * @property email The email of the user.
 * @property passwordValidation The information that can be used to validate the password of the user.
 */
data class User(
    val id: Id,
    val username: Username,
    val email: Email,
    val passwordValidation: PasswordValidationInfo
)
