package gomoku.domain.game

import gomoku.domain.Id
import gomoku.domain.SerializableDomainModel
import gomoku.domain.game.board.Board
import kotlinx.datetime.Instant

data class Game(
    val id: Id,
    val state: GameState,
    val variant: GameVariant,
    val board: Board,
    val createdAt: Instant,
    val updatedAt: Instant,
    val hostId: Id,
    val guestId: Id
) : SerializableDomainModel
