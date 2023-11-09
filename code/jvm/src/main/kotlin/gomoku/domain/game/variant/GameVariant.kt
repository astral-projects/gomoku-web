package gomoku.domain.game.variant

import gomoku.domain.components.Id
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantName

/**
 * Represents a game variant.
 * @property id The unique identifier of the game variant.
 * @property name The name of the game variant.
 * @property openingRule The opening rule of the game variant.
 * @property boardSize The board size of the game variant.
 */
data class GameVariant(
    val id: Id,
    val name: VariantName,
    val openingRule: OpeningRule,
    val boardSize: BoardSize
)
