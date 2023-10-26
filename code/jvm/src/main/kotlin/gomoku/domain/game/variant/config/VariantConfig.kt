package gomoku.domain.game.variant.config

data class VariantConfig(val name: VariantName, val openingRule: OpeningRule, val boardSize: BoardSize)
