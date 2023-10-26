package gomoku.services.game

import gomoku.domain.SystemInfo
import gomoku.domain.components.Id
import gomoku.domain.game.Game
import gomoku.domain.game.GameLogic
import gomoku.domain.game.GamePoints
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.Variant
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.repository.GamesRepository
import gomoku.repository.transaction.TransactionManager
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service

@Service
class GamesService(
    val transactionManager: TransactionManager,
    private val clock: Clock,
    private val variants: List<Variant>
) {

    /**
     * Maps ids generated by the database to the variants implemented in the code by the configuration name,
     * which is unique.
     */
    private val gameVariantMap: Map<Id, Variant> by lazy {
        transactionManager.run { transaction ->
            val variantsConfig: List<VariantConfig> = variants.map { it.config }
            transaction.gamesRepository.insertVariants(variantsConfig)
            val gameVariants = transaction.gamesRepository.getVariants()
            if (gameVariants.isEmpty()) {
                throw NoVariantImplementationFoundException("No variants found in the database")
            }
            gameVariants.associateBy({ it.id }, { variants.first { v -> v.config.name === it.name } })
        }
    }

    init {
        gameVariantMap
    }

    /**
     * Gets the game with the given id if it exists.
     * If the game does not exist, returns an error.
     */
    fun getGameById(gameId: Id): GettingGameResult {
        return transactionManager.run {
            when (val game = (it.gamesRepository.getGameById(gameId))) {
                null -> failure(GettingGameError.GameNotFound)
                else -> success(game)
            }
        }
    }

    /**
     * Creates a game with the given variant and the given user as the host.
     * If the user is already in a game, returns an error.
     * If the user is already in a lobby, removes the user from the lobby and creates the game.
     * If the user is not in a lobby, creates a lobby and adds the user to it.
     */
    fun findGame(variantId: Id, userId: Id): GameCreationResult =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val variant = gameVariantMap[variantId]
                ?: return@run failure(GameCreationError.VariantNotFound)
            val game = gamesRepository.findIfUserIsInGame(userId)
            val lobbyWaiting = gamesRepository.checkIfUserIsInLobby(userId)
            if (lobbyWaiting != null) {
                return@run success(FindGameSuccess.StillInLobby(lobbyWaiting.lobbyId))
            }
            if (game == null) {
                val lobby = gamesRepository.isMatchmaking(variantId, userId)
                if (lobby != null) {
                    if (!gamesRepository.deleteUserFromLobby(lobby.lobbyId)) {
                        failure(GameCreationError.UserAlreadyNotInLobby)
                    } else {
                        val gameId = gamesRepository.createGame(
                            variantId = variantId,
                            hostId = lobby.userId,
                            guestId = userId,
                            lobbyId = lobby.lobbyId,
                            board = variant.initialBoard()
                        )
                        when (gameId) {
                            null -> failure(GameCreationError.ErrorCreatingGame)
                            else -> success(FindGameSuccess.GameMatch(gameId))
                        }
                    }
                } else {
                  //  gamesRepository.checkIfUserIsInLobby(userId)
                    //    ?: failure(GameCreationError.UserAlreadyNotInLobby)
                    when (val lobbyId = gamesRepository.addUserToLobby(variantId, userId)) {
                        null -> failure(GameCreationError.VariantNotFound)
                        else -> success(FindGameSuccess.LobbyCreated(lobbyId))
                    }
                }
            } else {
                failure(GameCreationError.UserAlreadyInGame(game.id))
            }
        }

    /**
     * Deletes the game with the given id if the user is the host.
     * If the game is in progress, returns an error.
     * If the user is not the host, returns an error.
     */
    fun deleteGame(gameId: Id, userId: Id): GamePutResult =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            gamesRepository.getGameById(gameId)
                ?: return@run failure(GamePutError.GameNotFound)
            gamesRepository.userIsTheHost(gameId, userId)
                ?: return@run failure(GamePutError.UserIsNotTheHost)
            when (val wasGameDeleted = gamesRepository.deleteGame(gameId, userId)) {
                false -> failure(GamePutError.GameIsInprogress)
                true -> success(wasGameDeleted)
            }
        }

    /**
     * Returns the system info.
     */
    fun getSystemInfo(): SystemInfo = SystemInfo

    /**
     * Makes a move in the game, passing the user id and the move.
     * If the user does not belong to the game, returns an error.
     * If the move is not valid, returns an error.
     * If the game is not found, returns an error.
     * Depending on the board type, updates the points of the players.Using function updatedPointsBasedOnBoardType
     * Finally, returns the updated game.Or returns an error
     */
    fun makeMove(gameId: Id, user: Id, square: Square): GameMakeMoveResult =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val game = gamesRepository.getGameById(gameId)
                ?: return@run failure(GameMakeMoveError.GameNotFound)
            gamesRepository.userBelongsToTheGame(user, gameId)
                ?: return@run failure(GameMakeMoveError.UserDoesNotBelongToThisGame)
            val variant = gameVariantMap[game.variant.id]
                ?: return@run failure(GameMakeMoveError.VariantNotFound)
            val gameLogic = GameLogic(variant, clock)
            when (val playLogic = gameLogic.play(square, game, user)) {
                is Failure -> failure(GameMakeMoveError.MoveNotValid(playLogic.value))
                is Success -> {
                    val updatedGame = playLogic.value
                    updatedPointsBasedOnBoardType(gamesRepository, variant.points, user, updatedGame)
                    when (val makeMove = gamesRepository.updateGame(gameId, updatedGame.board)) {
                        false -> failure(GameMakeMoveError.GameNotFound)
                        true -> success(makeMove)
                    }
                }
            }
        }

    /**
     * Exits the game with the given id if the user belongs to the game.
     * If the user is not in the game, returns an error.
     * If the game is already finished, returns an error.
     * If the user forfeits, the winner is the other user.
     * The points are updated based on the board type.
     */
    fun exitGame(gameId: Id, userId: Id): GameDeleteResult {
        return transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val game = gamesRepository.getGameById(gameId)
                ?: return@run failure(GameDeleteError.GameNotFound)
            gamesRepository.userBelongsToTheGame(userId, gameId)
                ?: return@run failure(GameDeleteError.UserDoesntBelongToThisGame)
            val winner = gamesRepository.exitGame(gameId, userId)
            if (winner != null) {
                val variant = gameVariantMap[game.variant.id]
                    ?: return@run failure(GameDeleteError.VariantNotFound)
                success(
                    gamesRepository.updatePoints(
                        winnerId = winner,
                        loserId = userId,
                        winnerPoints = variant.points.onForfeitOrTimer.winner,
                        loserPoints = variant.points.onForfeitOrTimer.forfeiter,
                        shouldCountAsGameWin = true
                    )
                )
            } else {
                failure(GameDeleteError.GameAlreadyFinished)
            }
        }
    }

    /**
     * This function updates the points of the players based on the board type.
     */
    private fun updatedPointsBasedOnBoardType(
        gamesRepository: GamesRepository,
        gamePoints: GamePoints,
        userId: Id,
        game: Game
    ): Boolean = when (game.board) {
        is BoardWin -> {
            gamesRepository.updatePoints(
                winnerId = userId,
                loserId = if (userId == game.hostId) game.guestId else game.hostId,
                winnerPoints = gamePoints.onFinish.winner,
                loserPoints = gamePoints.onFinish.loser,
                shouldCountAsGameWin = true
            )
        }

        is BoardDraw -> {
            gamesRepository.updatePoints(
                winnerId = userId,
                loserId = if (userId == game.hostId) game.guestId else game.hostId,
                winnerPoints = gamePoints.onDraw.shared,
                loserPoints = gamePoints.onDraw.shared,
                shouldCountAsGameWin = false
            )
        }

        is BoardRun -> true
    }

    fun waitForGame(lobbyId: Id, userId: Id): GameWaitResult =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            val lobby = gamesRepository.checkIfUserIsInLobby(userId)
            if (lobby != null) {
                if (lobby.lobbyId != lobbyId) {
                    return@run failure(GameWaitError.UserDoesNotBelongToThisLobby)
                }
                return@run success("Waiting in lobby ${lobby.lobbyId}")
            }
            // TODO(Try a better name )
            val foundLobby = gamesRepository.waitForGame(lobbyId, userId)
                ?: return@run failure(GameWaitError.UserDoesntBelongToAnyGameOrLobby)
            success(foundLobby.value.toString())
        }

    fun exitLobby(lobbyId: Id, userId: Id): LobbyDeleteResult =
        transactionManager.run { transaction ->
            val gamesRepository = transaction.gamesRepository
            when (val res = gamesRepository.deleteLobby(lobbyId, userId)) {
                true -> success(res)
                else -> failure(LobbyDeleteError.LobbyNotFound)
            }
        }

    fun getVariants(): GetVariantsResult =
        transactionManager.run {transaction->
            val gamesRepository = transaction.gamesRepository
            if (variants.isEmpty()) {
                return@run failure(GetVariantsError.VariantsEmpty)
            }
            success(gameVariantMap.map { GameVariant(it.key, it.value.config.name, it.value.config.openingRule, it.value.config.boardSize) })
        }
}
