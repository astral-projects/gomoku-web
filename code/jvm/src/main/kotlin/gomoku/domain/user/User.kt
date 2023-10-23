package gomoku.domain.user

import gomoku.domain.Id
import gomoku.domain.errors.GettingUserResult
import gomoku.domain.errors.InvalidUserError
import gomoku.utils.Failure
import gomoku.utils.Success

private const val MIN_USERNAME_LENGTH = 5
private const val MAX_USERNAME_LENGTH = 30

class User private constructor(
    val id: Id,
    val username: Username,
    val email: Email,
    val passwordValidation: PasswordValidationInfo
) {

    companion object {
        operator fun invoke(
            id: Id,
            username: Username,
            email: Email,
            passwordValidation: PasswordValidationInfo
        ): GettingUserResult {
            return when {
                username.value.isBlank() -> Failure(InvalidUserError.UserIsBlank)
                username.value.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH ->
                    Failure(InvalidUserError.InvalidLength)
                else -> Success(User(id, username, email, passwordValidation))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (username != other.username) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + email.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(id=$id,username=$username, email=$email, passwordValidation=$passwordValidation)"
    }
}
