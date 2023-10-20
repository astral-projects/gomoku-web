package gomoku.repository.jdbi

import gomoku.domain.Id
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.repository.jdbi.JdbiTestConfiguration.runWithHandle
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

// Don't forget to ensure DBMS is up (e.g. by running ./gradlew dbTestsWait)
class JdbiGameRepositoryTests {

    @Test
    fun `can create a game and get game by id`() = runWithHandle { handle ->
        //given: a GamesRepository
        val repo = JdbiGameRepository(handle)

        //when: creating a game
        val variantId = Id(1)
        val hostId = Id(1)
        val guestId = Id(2)
        val lobbyId = Id(1)
        val createdGameId = repo.createGame(variantId, hostId, guestId, lobbyId)

        //and: retrieving the game by id
        val retrievedGameById = repo.getGameById()



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