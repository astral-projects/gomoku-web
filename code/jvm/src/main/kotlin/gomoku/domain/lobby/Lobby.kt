package gomoku.domain.lobby

import gomoku.domain.game.GameVariant
import gomoku.domain.game.OpeningRule
import gomoku.domain.game.board.BoardSize

data class Lobby(
    val userId: Int,
    val variant: GameVariant,
    val openingRule: OpeningRule,
    val boardSize: BoardSize,
)