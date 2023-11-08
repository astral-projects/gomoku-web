package gomoku.domain.user.components

import gomoku.domain.components.Component
import gomoku.domain.components.GettingUsernameResult
import gomoku.domain.components.UsernameError
import gomoku.utils.Failure
import gomoku.utils.Success

private const val MAX_USERNAME_LENGTH = 30
private const val MIN_USERNAME_LENGTH = 5

/**
 * Component that provides a generic username container for domain objects.
 */
class Username private constructor(val value: String) : Component {

    companion object {
        operator fun invoke(value: String): GettingUsernameResult = when {
            value.isEmpty() -> Failure(UsernameError.EmptyUsername)
            value.isBlank() -> Failure(UsernameError.BlankUsername)
            value.length !in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH -> Failure(UsernameError.InvalidLength)
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
