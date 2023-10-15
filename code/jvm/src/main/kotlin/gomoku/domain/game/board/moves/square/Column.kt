package gomoku.domain.game.board.moves.square

/**
 * Represents a Column instance defined by a unique [letter].
 */
data class Column(val letter: Char) {
    init {
        require(letter in 'a'..'z') {
            "Column letter id must be between 'a' and 'z'"
        }
    }

    override fun toString(): String = letter.toString()
}
