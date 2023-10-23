package gomoku.domain.user

import gomoku.domain.errors.GettingEmailResult
import gomoku.domain.errors.InvalidEmailError
import gomoku.utils.Failure
import gomoku.utils.Success

class Email private constructor(
    val value: String
) {

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
