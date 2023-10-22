package gomoku.services.game

import gomoku.domain.Id

/**
 * Represents a successful operation result when finding a game.
 * Either a game was found or a lobby was created.
 * @param id The id of the game created or the lobby created.
 * @param message A message describing the result.
 */
data class FindGameSuccess(val id: Id, val message: String)