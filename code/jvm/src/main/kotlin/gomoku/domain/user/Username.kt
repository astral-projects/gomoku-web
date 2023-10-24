package gomoku.domain.user

import gomoku.domain.errors.GettingUsernameResult
import gomoku.domain.errors.InvalidUsernameError
import gomoku.utils.Failure
import gomoku.utils.Success

private const val MAX_USERNAME_LENGTH = 30
private const val MIN_USERNAME_LENGTH = 5

class Username private constructor(val value: String) {

    companion object {
        operator fun invoke(value: String): GettingUsernameResult = when {
            value.isBlank() -> Failure(InvalidUsernameError.BlankUsername)
            value.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH -> Failure(InvalidUsernameError.InvalidLength)
            else -> Success(Username(value))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Username) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode() = value.hashCode()

    override fun toString() = value
}
