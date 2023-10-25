package gomoku.domain.game.board.moves.square

/**
 * Represents a Column instance defined by a unique [letter].
 */
data class Column(val letter: Char) {

    // TODO(Get rid of the require)
    init {
        require(letter in 'a'..'z') {
            "Column letter value must be between 'a' and 'z'"
        }
    }

    override fun toString(): String = letter.toString()

    fun toIndex(): Int = letter - 'a'
}
