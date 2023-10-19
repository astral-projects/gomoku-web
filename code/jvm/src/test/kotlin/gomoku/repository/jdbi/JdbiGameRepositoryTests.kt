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
import kotlin.test.fail

// Don't forget to ensure DBMS is up (e.g. by running ./gradlew dbTestsWait)
class JdbiGameRepositoryTests {

    @Test
    fun `can make a move`() = runWithHandle { handle ->

        val repo = JdbiGameRepository(handle)

        val game = repo.getGameById(Id(1))
            ?: fail("Game not found")
        val grid = game.board.grid + Move(Square(Column('c'), Row(1)), Piece(Player.b))
        require(game.board is BoardRun)
        val newBoard = game.board.copy(grid = grid)
        repo.updateGame(game.id, newBoard)

    }
}
