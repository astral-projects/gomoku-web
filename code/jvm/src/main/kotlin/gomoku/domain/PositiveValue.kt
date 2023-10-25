package gomoku.domain

import gomoku.domain.errors.InvalidPositiveValueError
import gomoku.domain.errors.PositiveValueResult
import gomoku.utils.Failure
import gomoku.utils.Success

class PositiveValue private constructor(val value: Int): Component {
    init {
        require(value > 0) { "Value must be positive" }
    }

    companion object {
        operator fun invoke(value: Int): PositiveValueResult =
            if (value > 0) Success(PositiveValue(value))
            else Failure(InvalidPositiveValueError.InvalidPositiveValue(value))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PositiveValue) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value

    override fun toString()= "PositiveValue(value=$value)"
}
