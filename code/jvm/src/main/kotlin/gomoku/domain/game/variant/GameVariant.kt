package gomoku.domain.game.variant

import gomoku.domain.Id
import gomoku.domain.game.board.BoardSize

data class GameVariant(val id: Id, val name: VariantName, val openingRule: OpeningRule, val boardSize: BoardSize)
