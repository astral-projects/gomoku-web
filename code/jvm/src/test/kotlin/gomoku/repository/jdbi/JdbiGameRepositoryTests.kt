package gomoku.repository.jdbi

import gomoku.TestDataGenerator.newTestEmail
import gomoku.TestDataGenerator.newTestUserName
import gomoku.TestDataGenerator.newTokenValidationData
import gomoku.domain.Id
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.user.PasswordValidationInfo
import gomoku.repository.jdbi.JdbiTestConfiguration.runWithHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class JdbiGameRepositoryTests {
/*
    @Test
    fun `can create a game, get game and variant and then delete the game`() = runWithHandle { handle ->
        //given: a GamesRepository and a UsersRepository
        val repoGames = JdbiGameRepository(handle)
        val repoUsers = JdbiUsersRepository(handle)

        //when: creating a host waiting for a game
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val host = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        //and: creating a guest waiting for a game
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guest = repoUsers.storeUser(username2, email2, passwordValidationInfo2)


        //and: choosing a variant
        val variantId = Id(1)
        val variant = repoGames.getVariantById(variantId)
        //then:
        assertNotNull(variant)
        assertEquals(variantId, variant.id)

        //and: creating a game
        val createdGameId = repoGames.createGame(variant.id, host, guest, Id(5))
        //then:
        assertNotNull(createdGameId)

        //and: retrieving the game by id
        val retrievedGameById = repoGames.getGameById(createdGameId)

        //then:
        assertNotNull(retrievedGameById)
        assertEquals(createdGameId, retrievedGameById.id)
        assertEquals(retrievedGameById.hostId, host)
        assertEquals(retrievedGameById.guestId, guest)
        assertEquals(retrievedGameById.variant, variant)


        //and: trying to delete a game with the guest id
        val isGameDeleted = repoGames.deleteGame(createdGameId, guest)

        //then:
        assertFalse(isGameDeleted)

        //and: trying to delete a game with the guest id
        val isGameDeletedByHost = repoGames.deleteGame(createdGameId, host)

        //then:
        assertTrue(isGameDeletedByHost)

        //and: getting again the game
        val gameAfterDeleted = repoGames.getGameById(createdGameId)

        //then:
        assertNull(gameAfterDeleted)

    }

    @Test
    fun `user enters in a lobby`() = runWithHandle {
        //given: a GamesRepository and a UsersRepository
        val repoGames = JdbiGameRepository(it)
        val repoUsers = JdbiUsersRepository(it)

        //when: creating a host
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val variant = repoGames.getVariantById(Id(1))
        assertNotNull(variant)
        val host = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        //and: adding the user in the lobby
        val lobbyId = repoGames.addUserToLobby(variant.id, host)

        //then:
        assertNotNull(lobbyId)

        //and: checking if the user is waiting in the lobby
        val waitingLobby = repoGames.waitingInLobby(host)

        //then:
        assertNotNull(waitingLobby)
        assertEquals(lobbyId, waitingLobby.lobbyId)
        assertEquals(host, waitingLobby.userId)
        assertEquals(variant.id, waitingLobby.variantId)


        //and: deleting the user from the lobby
        val isUserDeleted = repoGames.deleteUserFromLobby(lobbyId)

        //then:
        assertTrue(isUserDeleted)

        //and: checking if the user is waiting in the lobby again
        val waitingLobbyAfterDelete = repoGames.waitingInLobby(host)

        //then:
        assertNull(waitingLobbyAfterDelete)

    }

    @Test
    fun `there is already a user waiting in lobby with the variant choose for user created`() = runWithHandle {
        //given: a GamesRepository and a UsersRepository
        val repoGames = JdbiGameRepository(it)
        val repoUsers = JdbiUsersRepository(it)
        //when: a user waiting in lobby
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val variant = repoGames.getVariantById(Id(4))
        assertNotNull(variant)
        val host = repoUsers.storeUser(username1, email1, passwordValidationInfo1)
        val lobbyId = repoGames.addUserToLobby(variant.id, host)
        assertNotNull(lobbyId)
        //and: creating another user to enter in lobby
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guest = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        //when: adding the guest in the lobby
        val lobbyId2 = repoGames.addUserToLobby(variant.id, guest)

        //then:
        assertNotNull(lobbyId2)

        //and: isMatchmaking to see if there is a user waiting in a lobby with the variant choose for user created
        val isMatchmaking = repoGames.isMatchmaking(variant.id, guest)

        //then:
        assertNotNull(isMatchmaking)
        assertEquals(lobbyId, isMatchmaking.lobbyId)
        assertEquals(host, isMatchmaking.userId)
        assertEquals(variant.id, isMatchmaking.variantId)
        //and: deleting the users from the lobby

        val isUserDeleted = repoGames.deleteUserFromLobby(lobbyId)
        val isUserDeleted2 = repoGames.deleteUserFromLobby(lobbyId)

        //then:
        assertTrue(isUserDeleted)
        assertFalse(isUserDeleted2)
    }

    @Test
    fun `get system info`() = runWithHandle {
        //given: a repository
        val repo = JdbiGameRepository(it)

        //when: getting system info
        val systemInfo = repo.getSystemInfo()
        val authorsQuantity = 3

        //then:
        assertNotNull(systemInfo)
        assertEquals("1.0.9", systemInfo.VERSION)
        assertEquals("Gomoku Royale", systemInfo.GAME_NAME)
        assertEquals(authorsQuantity, systemInfo.authors.size)
    }

    @Test
    fun `check if `() = runWithHandle {


    }




    @Test
    fun `can make a move`() = runWithHandle { handle ->


        val repo = JdbiGameRepository(handle)
//        repo.updatePoints(Id(1), Id(1))
        val game = repo.getGameById(Id(1))
            ?: fail("Game not found")
        val grid = mapOf(
            Move(Square(Column('c'), Row(9)), Piece(Player.w)),
            Move(Square(Column('d'), Row(8)), Piece(Player.b)),
            Move(Square(Column('c'), Row(8)), Piece(Player.w)),
            Move(Square(Column('d'), Row(7)), Piece(Player.b)),
            Move(Square(Column('c'), Row(7)), Piece(Player.w)),
            Move(Square(Column('d'), Row(6)), Piece(Player.b)),
            Move(Square(Column('c'), Row(6)), Piece(Player.w)),
            Move(Square(Column('d'), Row(5)), Piece(Player.b))
        )
        require(game.board is BoardRun)
        val newBoard = game.board.copy(grid = grid)
        repo.updateGame(game.id, newBoard)
    }*/
}
