package gomoku.domain

/**
 * Provides a generic identifier for domain objects.
 */
class Id(val value: Int) {
    init {
        require(value > 0) { "Id must be greater than 0" }
    }
}
