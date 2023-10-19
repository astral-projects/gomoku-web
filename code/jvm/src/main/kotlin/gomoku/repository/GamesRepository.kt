package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Board
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.lobby.Lobby

interface GamesRepository {
    fun getGameById(id: Id): Game?

    // TODO fun getAllVariants(): List<GameVariant>
    fun getVariantById(variantId: Id): GameVariant?
    fun waitInLobby(variantId: Id, userId: Id): Boolean
    fun isMatchmaking(variantId: Id, userId: Id): Lobby?
    fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id): Boolean
    fun deleteUserFromLobby(userId: Id): Boolean
    fun deleteGame(gameId: Id, userId: Id): Boolean
    fun getSystemInfo(): SystemInfo
    fun userBelongsToTheGame(userId: Id, gameId: Id): Boolean
    fun updateGame(gameId: Id, board: Board): Boolean
    fun checkIfIsLobby(userId: Id): Boolean
    fun exitGame(gameId: Id, userId: Id): Boolean
    fun getGameStatus(gameId: Id, userId: Id): Game?
    fun userIsTheHost(userId: Id, gameId: Id): Boolean

    fun updatePoints(gameId: Id, userId: Id): Boolean
}
