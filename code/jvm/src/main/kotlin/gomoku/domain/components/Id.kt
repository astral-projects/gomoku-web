package gomoku.domain.components

import gomoku.utils.Failure
import gomoku.utils.Success

/**
 * Component that provides a generic identifier container for domain objects.
 */
class Id private constructor(val value: Int) : Component {

    companion object {
        operator fun invoke(value: Int): GettingIdResult {
            return if (value > 0) {
                Success(Id(value))
            } else {
                Failure(IdError.InvalidIdError(value))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Id) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value

    override fun toString() = "$value"
}
