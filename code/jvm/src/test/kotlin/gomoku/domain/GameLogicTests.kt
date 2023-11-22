package gomoku.domain

import gomoku.domain.components.Id
import gomoku.domain.game.Game
import gomoku.domain.game.GameLogic
import gomoku.domain.game.GameState
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantName
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.TestClock
import gomoku.utils.TestVariantImpl
import gomoku.utils.get
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class GameLogicTests {

    private val variant = TestVariantImpl
    private val clock = TestClock()
    private val hostId = Id(1).get()
    private val guestId = Id(2).get()
    private val variantId = Id(1).get()
    private val gameId = Id(1).get()
    private val startingGame = Game(
        id = gameId,
        variant = GameVariant(
            id = variantId,
            name = VariantName.TEST,
            openingRule = OpeningRule.NONE,
            boardSize = BoardSize.FIVE
        ),
        state = GameState.IN_PROGRESS,
        board = variant.initialBoard(),
        createdAt = clock.now(),
        updatedAt = clock.now(),
        hostId = hostId,
        guestId = guestId
    )

    @Test
    fun `can validate plays correctly`() {
        // given: a game logic
        val gameLogic = GameLogic(variant, clock)

        // when: the host makes a move (always the first player)
        val firstPlayResult = gameLogic.play(
            game = startingGame,
            userId = hostId,
            toSquare = Square("a1")
        )

        // then the move is valid
        when (firstPlayResult) {
            is Failure -> fail("Unexpected $firstPlayResult")
            is Success -> {
                assertEquals(startingGame.id, firstPlayResult.value.id)
                assertEquals(GameState.IN_PROGRESS, firstPlayResult.value.state)
                assertIs<BoardRun>(firstPlayResult.value.board)
            }
        }

        // and: the game board is updated
        val firstPlayBoard = firstPlayResult.value.board
        println(firstPlayBoard)

        // when: the other player makes a move
        val secondPlayResult = gameLogic.play(
            game = firstPlayResult.value,
            userId = guestId,
            toSquare = Square("a2")
        )

        // then: the move is valid
        when (secondPlayResult) {
            is Failure -> fail("Unexpected $secondPlayResult")
            is Success -> {
                assertEquals(startingGame.id, secondPlayResult.value.id)
                assertEquals(GameState.IN_PROGRESS, secondPlayResult.value.state)
                assertIs<BoardRun>(secondPlayResult.value.board)
            }
        }

        // when: the same user tries to make another move
        val thirdPlayResult = gameLogic.play(
            game = secondPlayResult.value,
            userId = guestId,
            toSquare = Square("a3")
        )

        // then: the move is invalid
        when (thirdPlayResult) {
            is Failure -> {
                assertIs<MakeMoveError.NotYourTurn>(thirdPlayResult.value)
            }

            is Success -> fail("Unexpected $thirdPlayResult")
        }

        // when: the other user tries to make a move on the same square
        val fourthPlayResult = gameLogic.play(
            game = secondPlayResult.value,
            userId = hostId,
            toSquare = Square("a2")
        )

        // then: the move is invalid
        when (fourthPlayResult) {
            is Failure -> {
                assertIs<MakeMoveError.PositionTaken>(fourthPlayResult.value)
            }

            is Success -> fail("Unexpected $fourthPlayResult")
        }

        // when: the other user tries to make a move on an invalid square
        val fightMove = Square("a${Column.MAX_INDEX}")
        val fifthPlayResult = gameLogic.play(
            game = secondPlayResult.value,
            userId = hostId,
            toSquare = fightMove
        )

        // then: the move is invalid
        when (fifthPlayResult) {
            is Failure -> {
                assertIs<MakeMoveError.InvalidPosition>(fifthPlayResult.value)
            }

            is Success -> fail("Unexpected $fifthPlayResult")
        }

        // when: the user tries to make another play
        val sixthPlayResult = gameLogic.play(
            game = secondPlayResult.value,
            userId = hostId,
            toSquare = Square("a3")
        )

        // then: the move is valid and the game finishes because of variant config
        when (sixthPlayResult) {
            is Failure -> fail("Unexpected $sixthPlayResult")
            is Success -> {
                assertEquals(startingGame.id, sixthPlayResult.value.id)
                assertEquals(GameState.FINISHED, sixthPlayResult.value.state)
                assertIs<BoardWin>(sixthPlayResult.value.board)
            }
        }

        // when: another move is made
        val seventhPlayResult = gameLogic.play(
            game = sixthPlayResult.value,
            userId = guestId,
            toSquare = Square("a4")
        )

        // then: the move is invalid because the game is over
        when (seventhPlayResult) {
            is Failure -> assertIs<MakeMoveError.GameOver>(seventhPlayResult.value)
            is Success -> fail("Unexpected $seventhPlayResult")
        }
    }
}
