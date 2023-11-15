package gomoku.domain.components

import gomoku.utils.Failure
import gomoku.utils.Success

// Constants
private const val MIN_TERM_LENGTH = 4

/**
 * Represents a search term.
 * @param value The term value.
 */
class Term private constructor(val value: String) : Component {

    companion object {
        operator fun invoke(value: String): GettingTermResult = when {
            value.length < MIN_TERM_LENGTH -> Failure(TermError.InvalidLength)
            else -> Success(Term(value))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Term) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString() = value

}