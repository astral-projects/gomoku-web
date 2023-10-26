package gomoku.domain.components

import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that provides a generic positive value container for domain objects.
 */
class PositiveValue private constructor(val value: Int) : Component {

    companion object {
        operator fun invoke(value: Int): PositiveValueResult =
            if (value > 0) {
                Success(PositiveValue(value))
            } else {
                Failure(PositiveValueError.InvalidPositiveValue(value))
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PositiveValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value

    override fun toString() = "$value"
}
