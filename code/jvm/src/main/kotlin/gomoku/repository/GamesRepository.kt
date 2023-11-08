package gomoku.repository

import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.game.Game
import gomoku.domain.game.board.Board
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.domain.game.variant.config.VariantName
import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.domain.lobby.Lobby
import java.util.*

/**
 * Repository for managing game related data.
 */
interface GamesRepository {

    /**
     * Retrieves a game by its id.
     * @param gameId the id of the game to retrieve.
     * @return the game with the given id, or null if no such game exists
     */
    fun getGameById(gameId: Id): Game?

    /**
     * Inserts a list of variants.
     * @param variants the list of [VariantConfig]s to insert.
     * @return true if the variants were inserted successfully, false otherwise.
     */
    fun insertVariants(variants: List<VariantConfig>): Boolean

    /**
     * Retrieves a list of all available variants.
     * @return a list of [GameVariant]s.
     */
    fun getVariants(): List<GameVariant>

    /**
     * Retrieves a variant by its name.
     * @param variantName the name of the variant to retrieve.
     * @return the variant id with the given name, or null if no such variant exists.
     */
    fun getVariantByName(variantName: VariantName): Id?

    /**
     * Creates a new game.
     * @param variantId the id of the variant to create the game with.
     * @param hostId the id of the host of the game.
     * @param guestId the id of the guest of the game.
     * @param lobbyId the id of the lobby.
     * @param board the initial board of the game.
     * @return the id of the created game, or null if the game could not be created.
     */
    fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id, board: Board): Id?

    /**
     * Deletes a game.
     * @param gameId the id of the game to delete.
     * @param userId the id of the user requesting the deletion.
     * @return true if the game was deleted successfully, false otherwise.
     */
    fun deleteGame(gameId: Id, userId: Id): Boolean

    /**
     * Adds a user to a lobby given a variant id.
     * @param variantId the id of the variant to be associated.
     * @param userId the id of the user to be added.
     * @return the id of the lobby the user was added to, or null if the user could not be added.
     */
    fun addUserToLobby(variantId: Id, userId: Id): Id?

    /**
     * Removes a user from a lobby.
     * @param lobbyId the id of the lobby to remove the user from.
     * @return true if the user was removed successfully, false otherwise.
     */
    fun deleteUserFromLobby(lobbyId: Id): Boolean

    /**
     * Asserts if a user is in a lobby already waiting for a game with the given variant.
     * @param variantId the id of the variant to search for.
     * @param userId the id of the user to search for.
     * @return the lobby where the host is waiting for a game with the given variant, or null if no such lobby exists.
     */
    fun isMatchmaking(variantId: Id, userId: Id): Lobby?

    /**
     * Asserts if a user is in a game already.
     * @param userId the id of the user to search for.
     * @return the game with the given id if the user is in the game, or null if no such game exists.
     */
    fun findIfUserIsInGame(userId: Id): Game?

    /**
     * Asserts if a user is playing a given game.
     * @param userId the id of the user to search for.
     * @param gameId the id of the game to search for.
     * @return the game with the given id if the user is in the game, or null if no such game exists.
     */
    fun userBelongsToTheGame(userId: Id, gameId: Id): Game?

    /**
     * Updates the board of a given game.
     * @param gameId the id of the game to update.
     * @param board the new board to update the game with.
     * @return true if the board was updated successfully, false otherwise.
     */
    fun updateGame(gameId: Id, board: Board): Boolean

    /**
     * Asserts if a user is already waiting in a lobby for a game to start.
     * @param userId the id of the user to search for.
     * @return the lobby where the user is waiting, or null if no such lobby exists.
     */
    fun checkIfUserIsInLobby(userId: Id): Lobby?

    /**
     * Allows a user to exit a game and marks the game as finished.
     * @param gameId the id of the game to search for.
     * @param userId the id of the user to search for.
     * @return the id of the game that was exited, or null if no such game exists.
     */
    fun exitGame(gameId: Id, userId: Id): Id?

    /**
     * Asserts if a user is the host of a given game.
     * @param gameId the id of the game to search for.
     * @param userId the id of the user to search for.
     * @return the game with the given id if the user is the host, or null if no such game exists.
     */
    fun userIsTheHost(gameId: Id, userId: Id): Game?

    /**
     * Updates the points of a given game.
     * @param winnerId the id of the winner of the game.
     * @param loserId the id of the loser of the game.
     * @param winnerPoints the points to be added to the winner.
     * @param loserPoints the points to be added to the loser.
     * @param shouldCountAsGameWin whether the game should count as a win for the winner. If false,
     * the game will count as a draw for both players.
     * @return true if the points were updated successfully, false otherwise.
     */
    fun updatePoints(
        winnerId: Id,
        loserId: Id,
        winnerPoints: NonNegativeValue,
        loserPoints: NonNegativeValue,
        shouldCountAsGameWin: Boolean
    ): Boolean

    /**
     * Asserts if a user is waiting in a lobby or if is already in a game to start.
     * @param lobbyId the id of the lobby to search for.
     * @param userId the id of the user to search for.
     * @return id of the game if the game is ready to start, null otherwise.
     */
    fun waitForGame(lobbyId: Id, userId: Id): Id?


    /**
     * Deletes a lobby.
     * @param lobbyId the id of the lobby to delete.
     * @param userId the id of the user requesting the deletion.
     * @return true if the lobby was deleted successfully, false otherwise.
     */
    fun deleteLobby(lobbyId: Id, userId: Id): Boolean

    /**
     * Asserts if an idempotency key has been used or not.
     * @param idempotencyKey the idempotency key to search for.
     * @param gameId the id of the game to search for.
     * @return true if the idempotency key has been used, false otherwise.
     */
    fun isIdempotencyKeyUsed(idempotencyKey: IdempotencyKey, gameId: Id): Boolean

    /**
     * Marks an idempotency key as used.
     * @param idempotencyKey the idempotency key to mark as used.
     * @param gameId the id of the game that key was used.
     */
    fun markIdempotencyKeyAsUsed(idempotencyKey: IdempotencyKey, gameId: Id)

    /**
     * Retrieves the info of an idempotency key, gameId, expiration and the key.
     * @param idempotencyKey the idempotency key to search for.
     */
    fun getIdempotencyKeyInfo(idempotencyKey: UUID): IdempotencyKey?
}
