package gomoku.domain.components

import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that provides a generic non-negative value container for domain objects.
 */
class NonNegativeValue private constructor(val value: Int) : Component {

    companion object {
        operator fun invoke(value: Int): NonNegativeValueResult {
            return if (value < 0) {
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

    override fun hashCode(): Int = value

    override fun toString() = "$value"
}
