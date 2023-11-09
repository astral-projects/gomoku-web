package gomoku.domain.game.variant.config

/**
 * Represents a game variant configuration.
 * @property name The name of the game variant.
 * @property openingRule The opening rule of the game variant.
 * @property boardSize The board size of the game variant.
 */
data class VariantConfig(
    val name: VariantName,
    val openingRule: OpeningRule,
    val boardSize: BoardSize
)
