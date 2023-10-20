package gomoku.domain.game.variant

import gomoku.domain.game.board.BoardSize

data class VariantConfig(val name: VariantName, val openingRule: OpeningRule, val boardSize: BoardSize)
