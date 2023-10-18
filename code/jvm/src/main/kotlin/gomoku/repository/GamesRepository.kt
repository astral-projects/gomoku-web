package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.lobby.Lobby
import gomoku.domain.user.User
import gomoku.repository.jdbi.model.game.JdbiGameJoinVariantModel
import gomoku.repository.jdbi.model.lobby.JdbiLobbyModel

interface GamesRepository {
    fun getGameById(id: Id): Game?
    // TODO fun getAllVariants(): List<GameVariant>
    // TODO fun getVariantById(variantId: Id): GameVariant?
    fun insertInLobby(variantId: Id, userId: Id): Boolean
    fun isMatchmaking(variantId: Id,userId: Id): Lobby?
    fun createGame(variantId: Id, hostId: Id,guestId: Id, lobbyId: Id): Boolean

    fun checkIfIsLobby(userId: Id): Boolean

    fun deleteUserFromLobby(userId: Id): Boolean
    fun deleteGame(game: Id, userId: Id): Boolean
    fun getSystemInfo(): SystemInfo
    fun userBelongsToTheGame(user: User, gameId: Id): Boolean
    fun makeMove(id: Id, userId: Id, square: Square, player: Player): Boolean
    fun exitGame(id: Id, user: User): Boolean
    fun getGameStatus(gameId: Id, user: User): Game?
    fun userIsTheHost(userId: Id, gameId: Id): Boolean

    fun updatePoints(gameId: Id,userId: Id): Boolean
}
