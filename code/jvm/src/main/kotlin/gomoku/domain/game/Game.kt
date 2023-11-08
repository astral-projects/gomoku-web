package gomoku.domain.game

import gomoku.domain.components.Id
import gomoku.domain.game.board.Board
import gomoku.domain.game.variant.GameVariant
import kotlinx.datetime.Instant

/**
 * Represents a game.
 * @property id The unique identifier of the game.
 * @property state The state of the game.
 * @property variant The variant associated with the game.
 * @property board The game board.
 * @property createdAt The [Instant] at which the game was created.
 * @property updatedAt The [Instant] at which the game was last updated.
 * @property hostId The unique identifier of the host player.
 * @property guestId The unique identifier of the guest player.
 */
data class Game(
    val id: Id,
    val state: GameState,
    val variant: GameVariant,
    val board: Board,
    val createdAt: Instant,
    val updatedAt: Instant,
    val hostId: Id,
    val guestId: Id
)
