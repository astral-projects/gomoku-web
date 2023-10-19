package gomoku.domain

import gomoku.domain.errors.InvalidIdError
import gomoku.utils.Either
import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Provides a generic identifier container for domain objects.
 */
class Id private constructor(val value: Int) {
    init {
        require(value > 0) { "Value must be positive to be considered a valid identifier" }
    }

    companion object {
        operator fun invoke(value: Int) = if (value > 0) Success(value) else Failure(InvalidIdError)
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
}
