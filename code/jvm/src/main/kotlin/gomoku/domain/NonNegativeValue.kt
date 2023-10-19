package gomoku.domain

data class NonNegativeValue(val value: Int) {
    init {
        require(value >= 0) { "Value must be non-negative" }
    }
}
