package gomoku.domain

data class PositiveValue(val value: Int) {
    init {
        require(value > 0) { "Value must be positive" }
    }
}
