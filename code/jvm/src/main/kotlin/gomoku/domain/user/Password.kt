package gomoku.domain.user

import gomoku.domain.Component
import gomoku.domain.errors.GettingPasswordResult
import gomoku.domain.errors.InvalidPasswordError
import gomoku.utils.Failure
import gomoku.utils.Success

private const val MAX_PASSWORD_LENGTH = 40
private const val MIN_PASSWORD_LENGTH = 8

class Password private constructor(
    val value: String
) : Component {

    companion object {
        private fun isSafe(value: String) = value.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH

        operator fun invoke(value: String): GettingPasswordResult {
            return if (!isSafe(value)) {
                Failure(InvalidPasswordError.PasswordNotSafe)
            } else if (value.isBlank() && value.isEmpty()) {
                Failure(InvalidPasswordError.PasswordIsEmpty)
            } else {
                Success(Password(value))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Password) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString() = value
}
