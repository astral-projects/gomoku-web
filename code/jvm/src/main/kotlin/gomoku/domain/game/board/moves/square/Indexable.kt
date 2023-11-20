package gomoku.domain.game.board.moves.square

/**
 * Represents an indexable object.
 */
@FunctionalInterface
fun interface Indexable {
    /**
     * Returns the index of the object.
     */
    fun toIndex(): Int
}
