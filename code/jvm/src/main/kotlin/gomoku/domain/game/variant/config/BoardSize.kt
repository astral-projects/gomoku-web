package gomoku.domain.game.variant.config

/**
 * Represents a board size.
 * @property size The size of the board.
 */
enum class BoardSize(val size: Int) {
    FIFTEEN(15),
    NINETEEN(19),
    SIX(6), // used for testing
    FIVE(5); // used for testing

    companion object {
        fun fromSize(size: Int): BoardSize = entries.find { it.size == size }
            ?: throw IllegalArgumentException("Board size $size is not supported")
    }
}
