package gomoku.repository.jdbi

import gomoku.TestClock
import gomoku.TestDataGenerator.newTestEmail
import gomoku.TestDataGenerator.newTestUserName
import gomoku.TestDataGenerator.newTokenValidationData
import gomoku.domain.Id
import gomoku.domain.game.variant.FreestyleVariant
import gomoku.domain.game.variant.VariantName
import gomoku.domain.user.PasswordValidationInfo
import gomoku.repository.jdbi.JdbiTestConfiguration.runWithHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JdbiGameRepositoryTests {

    companion object {
        private val clock = TestClock()
    }

    @Test
    fun `can create a game with a certain variant, get that game status and see who is the host`() = runWithHandle { handle ->
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

        //and: choosing a variant and a board
        val freestyle = FreestyleVariant()
        val board = freestyle.initialBoard()

        //and: adding that variant to the database
        val insertVariants = repoGames.insertVariants(listOf(freestyle.config))
        assertTrue(insertVariants)
        assertEquals(1, repoGames.getVariants().size)

        //and: getting the variant by id
        val variantId = repoGames.getVariantByName(VariantName.FREESTYLE)

        //then:
        assertNotNull(variantId)
        assertEquals(repoGames.getVariants()[0].id, variantId)


        //and: creating a game
        val createdGameId = repoGames.createGame(variantId, host, guest, Id(5), board)
        //then:
        assertNotNull(createdGameId)

        //and: retrieving the game by id
        val retrievedGameById = repoGames.getGameById(createdGameId)

        //then:
        assertNotNull(retrievedGameById)
        assertEquals(createdGameId, retrievedGameById.id)
        assertEquals(retrievedGameById.hostId, host)
        assertEquals(retrievedGameById.guestId, guest)
        assertEquals(retrievedGameById.variant.id, variantId)

        //when: seeing if the guest and host belong to the game
        val guestBelongsToGame = repoGames.userBelongsToTheGame(guest, createdGameId)
        val hostBelongsToGame = repoGames.userBelongsToTheGame(host, createdGameId)
        val randomUserBelongsToGame = repoGames.userBelongsToTheGame(Id(100), createdGameId)

        //then:
        assertTrue(guestBelongsToGame)
        assertTrue(hostBelongsToGame)
        assertFalse(randomUserBelongsToGame)

        //and: checking who is the host
        val isHost = repoGames.userIsTheHost(host, createdGameId)
        val isGuest = repoGames.userIsTheHost(guest, createdGameId)

        //then:
        assertTrue(isHost)
        assertFalse(isGuest)

        //and: getting game status for the host and guest and a random user
        val gameStatus = repoGames.getGameStatus(createdGameId, host)
        val gameStatus2 = repoGames.getGameStatus(createdGameId, guest)
        val gameStatus3 = repoGames.getGameStatus(createdGameId, Id(100))

        //then:
        assertNotNull(gameStatus)
        assertEquals(gameStatus.id, createdGameId)
        assertEquals(gameStatus.hostId, host)
        assertEquals(gameStatus.guestId, guest)
        assertEquals(gameStatus.variant.id, variantId)
        assertNotNull(gameStatus2)
        assertEquals(gameStatus2.id, createdGameId)
        assertEquals(gameStatus2.hostId, host)
        assertEquals(gameStatus2.guestId, guest)
        assertEquals(gameStatus2.variant.id, variantId)
        assertNull(gameStatus3)

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

        repoGames.deleteVariant(VariantName.FREESTYLE)

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
        val host = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        //and: choosing a variant and a board
        val freestyle = FreestyleVariant()

        //and: adding that variant to the database
        val insertVariants = repoGames.insertVariants(listOf(freestyle.config))
        assertTrue(insertVariants)
        assertEquals(1, repoGames.getVariants().size)

        //and: getting the variant by id
        val variantId = repoGames.getVariantByName(VariantName.FREESTYLE)

        //then:
        assertNotNull(variantId)
        assertEquals(repoGames.getVariants()[0].id, variantId)
        assertNotNull(variantId)

        //and: adding the user in the lobby
        val lobbyId = repoGames.addUserToLobby(variantId, host)

        //then:
        assertNotNull(lobbyId)

        //and: checking if the user is waiting in the lobby
        val isUserInLobby = repoGames.checkIfUserIsInLobby(host)

        //then:
        assertNotNull(isUserInLobby)
        assertEquals(lobbyId, isUserInLobby.lobbyId)
        assertEquals(host, isUserInLobby.userId)
        assertEquals(variantId, isUserInLobby.variantId)

        //and: deleting the user from the lobby
        val isUserDeleted = repoGames.deleteUserFromLobby(lobbyId)

        //then:
        assertTrue(isUserDeleted)

        //and: checking if the user is waiting in the lobby again
        val userInLobbyAfterDeleted = repoGames.checkIfUserIsInLobby(host)

        //then:
        assertNull(userInLobbyAfterDeleted)

        repoGames.deleteVariant(VariantName.FREESTYLE)

    }

    @Test
    fun `there is already a user waiting in lobby with the variant choose for user created`() = runWithHandle {
        //given: a GamesRepository and a UsersRepository
        val repoGames = JdbiGameRepository(it)
        val repoUsers = JdbiUsersRepository(it)

        //and: choosing a variant and a board
        val freestyle = FreestyleVariant()

        //and: adding that variant to the database
        repoGames.insertVariants(listOf(freestyle.config))

        //and: getting the variant by id
        val variantId = repoGames.getVariantByName(VariantName.FREESTYLE)

        //when: adding user in a lobby
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val host = repoUsers.storeUser(username1, email1, passwordValidationInfo1)

        val lobbyId = repoGames.addUserToLobby(variantId, host)
        assertNotNull(lobbyId)

        //and: creating another user to enter in a lobby
        val username2 = newTestUserName()
        val email2 = newTestEmail()
        val passwordValidationInfo2 = PasswordValidationInfo(newTokenValidationData())
        val guest = repoUsers.storeUser(username2, email2, passwordValidationInfo2)

        //when: adding the guest in the lobby
        val lobbyId2 = repoGames.addUserToLobby(variantId, guest)

        //then:
        assertNotNull(lobbyId2)

        //and: isMatchmaking to see if there is a user waiting in a lobby with the variant choose for user created
        val isMatchmaking = repoGames.isMatchmaking(variantId, guest)

        //then:
        assertNotNull(isMatchmaking)
        assertEquals(lobbyId, isMatchmaking.lobbyId)
        assertEquals(host, isMatchmaking.userId)
        assertEquals(variantId, isMatchmaking.variantId)

        //and: deleting the users from the lobby
        val isUserDeleted = repoGames.deleteUserFromLobby(lobbyId)
        val isUserDeleted2 = repoGames.deleteUserFromLobby(lobbyId2)

        //then:
        assertTrue(isUserDeleted)
        assertTrue(isUserDeleted2)
        repoGames.deleteVariant(VariantName.FREESTYLE)
    }

//    @Test
//    fun `can make a move`() = runWithHandle { handle ->
//
//
//        val repo = JdbiGameRepository(handle)
////        repo.updatePoints(Id(1), Id(1))
//        val game = repo.getGameById(Id(1))
//            ?: fail("Game not found")
//        val grid = mapOf(
//            Move(Square(Column('c'), Row(9)), Piece(Player.w)),
//            Move(Square(Column('d'), Row(8)), Piece(Player.b)),
//            Move(Square(Column('c'), Row(8)), Piece(Player.w)),
//            Move(Square(Column('d'), Row(7)), Piece(Player.b)),
//            Move(Square(Column('c'), Row(7)), Piece(Player.w)),
//            Move(Square(Column('d'), Row(6)), Piece(Player.b)),
//            Move(Square(Column('c'), Row(6)), Piece(Player.w)),
//            Move(Square(Column('d'), Row(5)), Piece(Player.b))
//        )
//        require(game.board is BoardRun)
//        val newBoard = game.board.copy(grid = grid)
//        repo.updateGame(game.id, newBoard)
//    }
}
