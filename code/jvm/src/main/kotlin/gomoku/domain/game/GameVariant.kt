package gomoku.domain.game

import gomoku.domain.Id
import gomoku.domain.game.board.BoardSize

data class GameVariant(val id: Id, val name: Variant, val openingRule: OpeningRule, val boardSize: BoardSize)
