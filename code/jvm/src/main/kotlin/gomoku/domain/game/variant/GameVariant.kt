package gomoku.domain.game.variant

import gomoku.domain.components.Id
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantName

data class GameVariant(val id: Id, val name: VariantName, val openingRule: OpeningRule, val boardSize: BoardSize)
