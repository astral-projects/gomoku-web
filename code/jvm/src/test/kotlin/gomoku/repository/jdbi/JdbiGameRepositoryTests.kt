package gomoku.repository.jdbi

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue
import gomoku.domain.game.GameState
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.variant.VariantName
import gomoku.domain.user.PasswordValidationInfo
import gomoku.repository.TestVariant
import gomoku.repository.jdbi.JdbiTestConfiguration.runWithHandle
import gomoku.utils.TestDataGenerator.newTestEmail
import gomoku.utils.TestDataGenerator.newTestUserName
import gomoku.utils.TestDataGenerator.newTokenValidationData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JdbiGameRepositoryTests {

    private val lobbyId = Id(Int.MAX_VALUE)

    @Test
    fun `can create a game, do inner verifications and user exiting the game`() = runWithHandle { handle ->
        // given: a game and users repository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        // when: creating a user to be the host
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val hostId = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        // and: creating a user to be the guest
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guestId = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        // and: choosing a variant and a board
        val variant = TestVariant()
        val board = variant.initialBoard()

        // and: adding that variant to the database
        val inserted = repoGames.insertVariants(listOf(variant.config))
        assertTrue(inserted)

        // and: getting the variant by name since its unique
        val variantId = repoGames.getVariantByName(VariantName.TEST)

        // then: variant is valid
        assertNotNull(variantId)
        assertEquals(variantId, repoGames.getVariants().find { it.name == VariantName.TEST }?.id)


        // when: creating a game
        val createdGameId = repoGames.createGame(
            variantId = variantId,
            hostId = hostId,
            guestId = guestId,
            lobbyId = lobbyId,
            board = board
        )

        // then: creation is successful
        assertNotNull(createdGameId)

        // when: retrieving the game by id
        val retrievedGameById = repoGames.getGameById(createdGameId)

        // then: the game is the same as the one created
        assertNotNull(retrievedGameById)
        assertEquals(createdGameId, retrievedGameById.id)
        assertEquals(retrievedGameById.hostId, hostId)
        assertEquals(retrievedGameById.guestId, guestId)
        assertEquals(retrievedGameById.variant.id, variantId)

        // when: seeing if the guest and host belong to the game
        val guestBelongsToGame = repoGames.userBelongsToTheGame(guestId, createdGameId)
        val hostBelongsToGame = repoGames.userBelongsToTheGame(hostId, createdGameId)
        val randomUserBelongsToGame = repoGames.userBelongsToTheGame(Id(Int.MAX_VALUE), createdGameId)

        // then:
        assertNotNull(guestBelongsToGame)
        assertEquals(guestBelongsToGame.id, createdGameId)
        assertNotNull(hostBelongsToGame)
        assertEquals(hostBelongsToGame.id, createdGameId)
        assertNull(randomUserBelongsToGame)

        // and: checking if the user is in a game
        val hostGame = repoGames.findIfUserIsInGame(hostId)
        val guestGame = repoGames.findIfUserIsInGame(guestId)

        // then:
        checkNotNull(hostGame)
        assertEquals(hostGame.id, createdGameId)
        checkNotNull(guestGame)
        assertEquals(guestGame.id, createdGameId)

        // and: checking who is the host
        val isHost = repoGames.userIsTheHost(createdGameId, hostId)
        val isGuest = repoGames.userIsTheHost(createdGameId, guestId)

        // then:
        assertNotNull(isHost)
        assertEquals(isHost.id, createdGameId)
        assertEquals(isHost.hostId, hostId)
        assertNull(isGuest)

        // when: the host exists the game
        val userStayedInGame = repoGames.exitGame(createdGameId, hostId)
        assertNotNull(userStayedInGame)
        assertEquals(userStayedInGame, guestId)

        // and: trying to delete a game with the guest id
        val isGameDeleted = repoGames.deleteGame(createdGameId, guestId)

        // then: guest cannot delete the game
        assertFalse(isGameDeleted)

        // when: trying to delete a game with the host id
        val isGameDeletedByHost = repoGames.deleteGame(createdGameId, hostId)

        // then: host can delete the game
        assertTrue(isGameDeletedByHost)

        // when: getting again the game
        val gameAfterDeleted = repoGames.getGameById(createdGameId)

        // then: the game is not found
        assertNull(gameAfterDeleted)

        handle.rollback()

    }

    @Test
    fun `user enters a lobby`() = runWithHandle { handle ->
        // given: a game and users repository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        // when: creating a host
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val host = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        // and: choosing a variant and a board
        val variant = TestVariant()

        // and: adding that variant to the database
        val insertVariants = repoGames.insertVariants(listOf(variant.config))
        assertTrue(insertVariants)

        // and: getting the variant by name since its unique
        val variantId = repoGames.getVariantByName(VariantName.TEST)

        // then: variant is valid
        assertNotNull(variantId)
        assertEquals(variantId, repoGames.getVariants().find { v -> v.name == VariantName.TEST }?.id)

        // when: adding the user in the lobby
        val lobbyId = repoGames.addUserToLobby(variantId, host)

        // then: a lobby is created
        assertNotNull(lobbyId)
        val isUserInLobby = repoGames.checkIfUserIsInLobby(host)
        assertNotNull(isUserInLobby)
        assertEquals(lobbyId, isUserInLobby.lobbyId)
        assertEquals(host, isUserInLobby.userId)
        assertEquals(variantId, isUserInLobby.variantId)

        // when: deleting the user from the lobby
        val isUserDeleted = repoGames.deleteUserFromLobby(lobbyId)

        // then: user is deleted
        assertTrue(isUserDeleted)

        // when: checking if the user is waiting in the lobby again
        val userInLobbyAfterDeleted = repoGames.checkIfUserIsInLobby(host)

        // then: user is not waiting in the lobby
        assertNull(userInLobbyAfterDeleted)

        handle.rollback()
    }

    @Test
    fun `users join and leave lobbies`() = runWithHandle { handle ->
        // given: a game and users repository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        // and: choosing a variant and a board
        val variant = TestVariant()

        // and: adding that variant to the database
        val inserted = repoGames.insertVariants(listOf(variant.config))
        assertTrue(inserted)

        // then: variant is valid
        val variantId = repoGames.getVariantByName(VariantName.TEST)
        assertEquals(variantId, repoGames.getVariants().find { v -> v.name == VariantName.TEST }?.id)

        // when: creating a host
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val hostId = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        // and: adding the host in the lobby
        val lobbyId = repoGames.addUserToLobby(variantId, hostId)

        // then: a lobby is created
        assertNotNull(lobbyId)

        // when: creating a guest
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guestId = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        // when: checking if another user is waiting in the lobby
        val matchMakingLobby = repoGames.isMatchmaking(variantId, guestId)

        // then:
        assertNotNull(matchMakingLobby)
        assertEquals(lobbyId, matchMakingLobby.lobbyId)
        assertEquals(hostId, matchMakingLobby.userId)
        assertEquals(variantId, matchMakingLobby.variantId)

        // when: deleting the users from the lobby
        val isUserDeleted = repoGames.deleteUserFromLobby(lobbyId)

        // then:
        assertTrue(isUserDeleted)

        handle.rollback()
    }

    @Test
    fun `creating and updating games`() = runWithHandle { handle ->
        // given: a game and users repository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        // when: creating a host waiting for a game
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val hostId = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        // and: creating a guest waiting for a game
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guestId = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        // and: choosing a variant and a board
        val variant = TestVariant()
        val board = variant.initialBoard()

        // and: adding that variant to the database
        val inserted = repoGames.insertVariants(listOf(variant.config))
        assertTrue(inserted)

        // when getting the variant by name since its unique
        val variantId = repoGames.getVariantByName(VariantName.TEST)

        // then: variant is valid
        assertNotNull(variantId)
        assertEquals(variantId, repoGames.getVariants().find { v -> v.name == VariantName.TEST }?.id)

        // when: creating a game
        val createdGameId = repoGames.createGame(variantId, hostId, guestId, lobbyId, board)
        val createdGameId2 = repoGames.createGame(variantId, hostId, guestId, Id(lobbyId.value - 1), board)

        // then: creation is successful
        assertNotNull(createdGameId)
        assertNotNull(createdGameId2)

        // when: getting the games by id
        val game1 = repoGames.getGameById(createdGameId)
        val game2 = repoGames.getGameById(createdGameId2)

        // then: both games are created
        assertNotNull(game1)
        assertEquals(createdGameId, game1.id)
        assertNotNull(game2)
        assertEquals(createdGameId2, game2.id)

        // and: both games are in progress state
        assertSame(game1.state, GameState.IN_PROGRESS)
        assertSame(game2.state, GameState.IN_PROGRESS)

        // when: updating game 1 with a new board
        val move: Square = Square(Column('a'), Row(1))
        val newBoard = board.play(move, variant)
        val updatedGame = repoGames.updateGame(game1.id, newBoard)

        // then: game 1 is updated and the board is different
        assertTrue(updatedGame)
        assertNotEquals(game1.board, newBoard)
        assertEquals(game1.board.grid.size + 1, newBoard.grid.size)

        // when: updating game 1 with a board that is won
        val boardWin = BoardWin(
            moves = newBoard.grid,
            winner = Player.w
        )
        val updatedGameWin = repoGames.updateGame(game1.id, boardWin)

        // then: update is successful and game 1 is finished
        assertTrue(updatedGameWin)
        val wonGame = repoGames.getGameById(game1.id)
        assertNotNull(wonGame)
        assertSame(wonGame.state, GameState.FINISHED)

        // when: updating game 2 with a board that is drawn
        val boardDraw = BoardDraw(moves = newBoard.grid)
        val updatedGameDraw = repoGames.updateGame(game2.id, boardDraw)

        // then: update is successful and game 2 is finished
        assertTrue(updatedGameDraw)
        val drawnGame = repoGames.getGameById(game2.id)
        assertNotNull(drawnGame)
        assertSame(drawnGame.state, GameState.FINISHED)

        // when: deleting both games
        repoGames.deleteGame(game1.id, hostId)
        repoGames.deleteGame(game2.id, hostId)

        // then: both games are deleted
        assertNull(repoGames.getGameById(game1.id))
        assertNull(repoGames.getGameById(game2.id))

        handle.rollback()
    }

    @Test
    fun `can update game points on a win-lose match`() = runWithHandle { handle ->
        // given a game and users repository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        // when: creating a host waiting for a game
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val hostId = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        // and: creating a guest waiting for a game
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guestId = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        // when: getting the current user stats
        val hostStats = repoUsers.getUserStats(hostId)
        val guestStats = repoUsers.getUserStats(guestId)

        // then: the stats exist
        assertNotNull(hostStats)
        assertNotNull(guestStats)

        // when: updating the stats
        val winnerPoints = NonNegativeValue(10)
        val loserPoints = NonNegativeValue(5)
        val updatedGame = repoGames.updatePoints(
            winnerId = hostId,
            loserId = guestId,
            winnerPoints = winnerPoints,
            loserPoints = loserPoints,
            shouldCountAsGameWin = true
        )

        // then: update was valid
        assertTrue(updatedGame)

        // when: getting the user's stats again
        val hostStatsAfterUpdate = repoUsers.getUserStats(hostId)
        val guestStatsAfterUpdate = repoUsers.getUserStats(guestId)

        // then: the host stats are updated
        assertNotNull(hostStatsAfterUpdate)
        assertEquals(hostStatsAfterUpdate.points, NonNegativeValue(hostStats.points.value + winnerPoints.value))
        assertEquals(hostStatsAfterUpdate.gamesPlayed, NonNegativeValue(hostStats.gamesPlayed.value + 1))
        assertEquals(hostStatsAfterUpdate.wins, NonNegativeValue(hostStats.wins.value + 1))
        assertEquals(hostStatsAfterUpdate.draws, NonNegativeValue(hostStats.draws.value))
        assertEquals(hostStatsAfterUpdate.losses, NonNegativeValue(hostStats.losses.value))

        // and: the guest stats are updated
        assertNotNull(guestStatsAfterUpdate)
        assertEquals(guestStatsAfterUpdate.points, NonNegativeValue(guestStats.points.value + loserPoints.value))
        assertEquals(guestStatsAfterUpdate.gamesPlayed, NonNegativeValue(guestStats.gamesPlayed.value + 1))
        assertEquals(guestStatsAfterUpdate.wins, NonNegativeValue(guestStats.wins.value))
        assertEquals(guestStatsAfterUpdate.draws, NonNegativeValue(guestStats.draws.value))
        assertEquals(guestStatsAfterUpdate.losses, NonNegativeValue(guestStats.losses.value + 1))

        handle.rollback()

    }

    @Test
    fun `can update game points on a draw`() = runWithHandle { handle ->
        // given a game and users repository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        // when: creating a host waiting for a game
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val hostId = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        // and: creating a guest waiting for a game
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guestId = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        // when: getting the current user stats
        val hostStats = repoUsers.getUserStats(hostId)
        val guestStats = repoUsers.getUserStats(guestId)

        // then: the stats exist
        assertNotNull(hostStats)
        assertNotNull(guestStats)

        // when: updating the stats
        val winnerPoints = NonNegativeValue(10)
        val loserPoints = NonNegativeValue(5)
        val updatedGame = repoGames.updatePoints(
            winnerId = hostId,
            loserId = guestId,
            winnerPoints = winnerPoints,
            loserPoints = loserPoints,
            shouldCountAsGameWin = false // draw
        )

        // then: update was valid
        assertTrue(updatedGame)

        // when: getting the user's stats again
        val hostStatsAfterUpdate = repoUsers.getUserStats(hostId)
        val guestStatsAfterUpdate = repoUsers.getUserStats(guestId)

        // then: the host stats are updated
        assertNotNull(hostStatsAfterUpdate)
        assertEquals(hostStatsAfterUpdate.points, NonNegativeValue(hostStats.points.value + winnerPoints.value))
        assertEquals(hostStatsAfterUpdate.gamesPlayed, NonNegativeValue(hostStats.gamesPlayed.value + 1))
        assertEquals(hostStatsAfterUpdate.wins, NonNegativeValue(hostStats.wins.value))
        assertEquals(hostStatsAfterUpdate.draws, NonNegativeValue(hostStats.draws.value + 1))
        assertEquals(hostStatsAfterUpdate.losses, NonNegativeValue(hostStats.losses.value))

        // and: the guest stats are updated
        assertNotNull(guestStatsAfterUpdate)
        assertEquals(guestStatsAfterUpdate.points, NonNegativeValue(guestStats.points.value + loserPoints.value))
        assertEquals(guestStatsAfterUpdate.gamesPlayed, NonNegativeValue(guestStats.gamesPlayed.value + 1))
        assertEquals(guestStatsAfterUpdate.wins, NonNegativeValue(guestStats.wins.value))
        assertEquals(guestStatsAfterUpdate.draws, NonNegativeValue(guestStats.draws.value + 1))
        assertEquals(guestStatsAfterUpdate.losses, NonNegativeValue(guestStats.losses.value))

        handle.rollback()

    }
}
