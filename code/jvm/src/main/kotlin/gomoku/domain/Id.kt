package gomoku.domain

/**
 * Provides a generic identifier container for domain objects.
 */
data class Id(val value: Int) {
    init {
        require(value > 0) { "Value must be positive to be considered a valid identifier" }
    }
}
