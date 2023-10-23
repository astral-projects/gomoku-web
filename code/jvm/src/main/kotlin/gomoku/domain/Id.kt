package gomoku.domain

import gomoku.domain.errors.GettingIdResult
import gomoku.domain.errors.InvalidIdError
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Provides a generic identifier container for domain objects.
 */
class Id (val value: Int) {

    companion object {
        operator fun invoke(value: Int): GettingIdResult = when {
            value <= 0 -> Failure(InvalidIdError.InvalidId)
            else -> Success(Id(value))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Id) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString() = "$value"
}
