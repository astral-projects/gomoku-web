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
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class JdbiGameRepositoryTests {

    @Test
    fun `can create a game, get game and variant and then delete the game`() = runWithHandle { handle ->
        //given: a GamesRepository
        val repo = JdbiGameRepository(handle)

        //when: creating a game
        val variantId = Id(1)
        val hostId = Id(1)
        val guestId = Id(2)
        val lobbyId = Id(6)
        val createdGameId = repo.createGame(variantId, hostId, guestId, lobbyId)

        //then:
        assertNotNull(createdGameId)

        //and: retrieving the game by id
        val retrievedGameById = repo.getGameById(createdGameId)

        //then:
        assertNotNull(retrievedGameById)
        assertEquals(createdGameId, retrievedGameById.id)

        //and: retrieving the variant by variantId
        val retrieveVariant = repo.getVariantById(variantId)
        assertNotNull(retrieveVariant)
        assertEquals(variantId, retrieveVariant.id)

        //and: deleting the game
        val isDeletedGame = repo.deleteGame(createdGameId, hostId)

        //and: getting again the game
        val deletedGame = repo.getGameById(createdGameId)

        //then:
        assertTrue(isDeletedGame)
        assertNull(deletedGame)

    }

    @Test
    fun `user choosing a variant and entering in a lobby, and then deleting the user from the lobby`() = runWithHandle {
        //given: a repository
        val repoGames = JdbiGameRepository(it)
        val repoUser = JdbiUsersRepository(it)

        //and: storing a user that chooses a variant
        val username1 = newTestUserName()
        val email1 = newTestEmail()
        val passwordValidationInfo1 = PasswordValidationInfo(newTokenValidationData())
        val host = repoUser.storeUser(username1, email1, passwordValidationInfo1)
        val variantChooseId = Id(1)

        val gameVariant = repoGames.getVariantById(Id(1))

        //then:
        assertNotNull(gameVariant)
        assertEquals(variantChooseId, gameVariant.id)

        //and: adding host to the lobby
        val lobby = repoGames.addUserToLobby(variantChooseId, host)

        //then: user is added
        assertNotNull(lobby)

        //and: check if the user is in a lobby
        val isUserInLobby = repoGames.checkIfIsLobby(host)

        //then:
        assertTrue(isUserInLobby)

        //and: deleting the user from a lobby
        val deleted = repoGames.deleteUserFromLobby(host)

        //then:
        assertTrue(deleted)

        //and: verify that the host in not in lobby anymore
        val isUserContinueInLobby = repoGames.checkIfIsLobby(host)

        //then:
        assertFalse(isUserContinueInLobby)



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
    }
}
