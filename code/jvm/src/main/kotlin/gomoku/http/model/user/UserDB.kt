package gomoku.http.model.user

import gomoku.domain.Id
import gomoku.domain.user.Email
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.Username

data class UserDB(
    val id: Int,
    val username: String,
    val email: String,
    val passwordValidation: String
)