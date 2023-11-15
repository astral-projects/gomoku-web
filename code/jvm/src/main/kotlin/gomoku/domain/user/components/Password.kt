package gomoku.domain.user.components

import gomoku.domain.components.Component
import gomoku.domain.components.GettingPasswordResult
import gomoku.domain.components.PasswordError
import gomoku.utils.Failure
import gomoku.utils.Success

// Constants
private const val MAX_PASSWORD_LENGTH = 40
private const val MIN_PASSWORD_LENGTH = 8

/**
 * Component that provides a generic password container for domain objects.
 */
class Password private constructor(
    val value: String
) : Component {

    companion object {
        private fun isSafe(value: String) = value.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH

        operator fun invoke(value: String): GettingPasswordResult {
            return if (value.isBlank()) {
                Failure(PasswordError.PasswordBlank)
            } else if (!isSafe(value)) {
                Failure(PasswordError.PasswordNotSafe)
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
