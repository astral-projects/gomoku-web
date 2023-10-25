package gomoku.domain.game.board.moves.square

data class Row(val number: Int) {
    //TODO(Get rid of the require)
    init {
        require(number > 0) { "Row number must be positive" }
    }
    override fun toString(): String = number.toString()

    fun toIndex(): Int = number - 1
}
