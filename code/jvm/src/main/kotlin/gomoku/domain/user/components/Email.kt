package gomoku.domain.user.components

import gomoku.domain.components.Component
import gomoku.domain.components.EmailError
import gomoku.domain.components.GettingEmailResult
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that provides a generic email container for domain objects.
 */
class Email private constructor(
    val value: String
) : Component {

    companion object {
        const val EMAIL_FORMAT = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"

        operator fun invoke(value: String): GettingEmailResult = when {
            value.matches(EMAIL_FORMAT.toRegex()) -> Success(Email(value))
            else -> Failure(EmailError.InvalidEmail)
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
