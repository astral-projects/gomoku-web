package gomoku.domain.user.components

import gomoku.domain.components.Component
import gomoku.domain.components.GettingEmailResult
import gomoku.domain.components.InvalidEmailError
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that provides a generic email container for domain objects.
 */
class Email private constructor(
    val value: String
) : Component {

    companion object {
        private const val emailFormat = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"

        operator fun invoke(value: String): GettingEmailResult = when {
            value.matches(emailFormat.toRegex()) -> Success(Email(value))
            else -> Failure(InvalidEmailError.InvalidEmail)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Email) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString() = value
}
