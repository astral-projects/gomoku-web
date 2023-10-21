package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.game.Game
import gomoku.domain.game.GameState
import gomoku.domain.game.board.Board
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.VariantConfig
import gomoku.domain.lobby.Lobby
import gomoku.utils.NotTested

interface GamesRepository {
    fun getGameById(gameid: Id): Game?

    @NotTested
    fun insertVariants(variants: List<VariantConfig>): Boolean

    @NotTested
    fun getVariants(): List<GameVariant>

    fun createGame(variantId: Id, hostId: Id, guestId: Id, lobbyId: Id, board: Board): Id?
    fun deleteGame(gameId: Id, userId: Id): Boolean
    fun addUserToLobby(variantId: Id, userId: Id): Id?
    fun deleteUserFromLobby(lobbyId: Id): Boolean

    @NotTested
    fun isMatchmaking(variantId: Id, userId: Id): Lobby?

    @NotTested
    fun userBelongsToTheGame(userId: Id, gameId: Id): Game?
    fun updateGame(gameId: Id, board: Board, gameState: GameState): Boolean
    fun checkIfUserIsInLobby(userId: Id): Lobby?
    fun exitGame(gameId: Id, userId: Id): Id?
    fun getGameStatus(gameId: Id, userId: Id): Game?

    @NotTested
    fun userIsTheHost(userId: Id, gameId: Id): Game?

    @NotTested
    fun updatePoints(
        gameId: Id,
        winnerId: Id,
        loserId: Id,
        winnerPoints: NonNegativeValue,
        loserPoints: NonNegativeValue,
        shouldCountAsGameWin: Boolean
    ): Boolean

    @NotTested
    fun findIfUserIsInGame(userId: Id): Game?
}
