package gomoku.domain.game.board.moves.square


data class Row(val number: Int) {
    init {
        require(number > 0) { "Row number must be positive" }
    }
    override fun toString(): String = number.toString()
}