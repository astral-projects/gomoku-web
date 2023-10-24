package gomoku.domain

import gomoku.domain.errors.NonNegativeValueError
import gomoku.domain.errors.NonNegativeValueResult
import gomoku.utils.Failure
import gomoku.utils.Success

class NonNegativeValue private constructor(val value: Int) {

    companion object {
        operator fun invoke(value: Int): NonNegativeValueResult {
            return if (value <= 0) {
                Failure(NonNegativeValueError.InvalidNonNegativeValue)
            } else {
                Success(NonNegativeValue(value))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NonNegativeValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString() = "$value"
}
