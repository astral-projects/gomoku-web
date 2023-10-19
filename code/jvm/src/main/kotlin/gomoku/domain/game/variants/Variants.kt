package gomoku.domain.game.variants

/**
 * Variants of the game.
 */
@FunctionalInterface
interface Variants {
    fun getVariant(variant: GameVariant): GameVariant
}
