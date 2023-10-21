package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.game.Game
import gomoku.domain.game.board.Board
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.VariantConfig
import gomoku.domain.game.variant.VariantName
import gomoku.domain.lobby.Lobby
import gomoku.utils.NotTested

/**
 * Interface for managing games and related operations in the API.
 * This repository provides methods to interact with game data, lobbies, and variants.
 */
interface GamesRepository {
    fun getGameById(id: Id): Game?
    fun insertVariants(variants: List<VariantConfig>): Boolean
    fun getVariants(): List<GameVariant>
    fun getVariantByName(variantName: VariantName): Id?
    fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id, board: Board): Id?
    fun deleteGame(gameId: Id, userId: Id): Boolean
    fun addUserToLobby(variantId: Id, userId: Id): Id?
    fun deleteUserFromLobby(lobbyId: Id): Boolean
    fun isMatchmaking(variantId: Id, userId: Id): Lobby?
    fun userBelongsToTheGame(userId: Id, gameId: Id): Game?
    fun checkIfUserIsInLobby(userId: Id): Lobby?
    fun getGameStatus(gameId: Id, userId: Id): Game?
    fun userIsTheHost(userId: Id, gameId: Id): Game?
    fun exitGame(gameId: Id, userId: Id): Id?
    fun findIfUserIsInGame(userId: Id): Game?
    fun updateGame(gameId: Id, board: Board): Boolean
    fun updatePoints(
        gameId: Id,
        winnerId: Id,
        loserId: Id,
        winnerPoints: NonNegativeValue,
        loserPoints: NonNegativeValue,
        shouldCountAsGameWin: Boolean
    ): Boolean
    fun deleteVariant(name: VariantName): Boolean // just for testing
    fun deleteLobby(lobbyId: Id): Boolean // just for testing
}
