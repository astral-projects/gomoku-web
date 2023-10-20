package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Board
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.lobby.Lobby
import gomoku.utils.NotTested

interface GamesRepository {
    fun getGameById(id: Id): Game?
    // TODO fun getAllVariants(): List<GameVariant>
    fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id): Id?
    fun getVariantById(variantId: Id): GameVariant?
    fun deleteGame(gameId: Id, userId: Id): Boolean
    fun addUserToLobby(variantId: Id, userId: Id): Id?
    fun deleteUserFromLobby(userId: Id): Boolean
    fun checkIfIsLobby(userId: Id): Boolean
    @NotTested
    fun isMatchmaking(variantId: Id, userId: Id): Lobby?
    @NotTested
    fun getSystemInfo(): SystemInfo
    @NotTested
    fun userBelongsToTheGame(userId: Id, gameId: Id): Boolean
    @NotTested
    fun updateGame(gameId: Id, board: Board): Boolean
    @NotTested
    fun exitGame(gameId: Id, userId: Id): Id?
    @NotTested
    fun getGameStatus(gameId: Id, userId: Id): Game?
    @NotTested
    fun userIsTheHost(userId: Id, gameId: Id): Boolean
    @NotTested
    fun updatePoints(gameId: Id, winnerId: Id, loserId:Id , winnerPoints:PositiveValue, loserPoints:PositiveValue , gamePoint:NonNegativeValue): Boolean
}
