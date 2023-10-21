package gomoku.services

import gomoku.TestClock
import gomoku.domain.Id
import gomoku.domain.game.GameLogic
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.variant.FreestyleVariant
import gomoku.repository.jdbi.JdbiTestConfiguration
import gomoku.repository.jdbi.transaction.JdbiTransactionManager
import gomoku.services.game.GamesService
import org.junit.jupiter.api.Test

class GameServicesTests {

    @Test
    fun `win a game`() {
        // given: a game service and a game
        val testClock = TestClock()
        val gamesService = createGamesService(testClock)

        // when: the game is won
        gamesService.makeMove(Id(1), Id(1), Square(Column('c'), Row(5)), Player.w)
    }
    companion object {

        private fun createGamesService(
            testClock: TestClock
        ) = GamesService(
            JdbiTransactionManager(JdbiTestConfiguration.jdbi),
            testClock,
            listOf(FreestyleVariant()),
        )
    }
}



