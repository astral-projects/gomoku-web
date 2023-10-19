package gomoku.domain.game.variants

import gomoku.domain.Id
import gomoku.domain.game.board.BoardSize

data class GameVariant(val id: Id, val name: AcceptableVariant, val openingRule: OpeningRule, val boardSize: BoardSize)
