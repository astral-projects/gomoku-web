package gomoku.domain.variant

import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.isFinished
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.variant.FreestyleVariant
import gomoku.utils.Failure
import gomoku.utils.Success
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class FreestyleVariantTest : VariantTest() {

    @Test
    override fun `can make moves on the board`() {
        // given: a variant
        val variant = FreestyleVariant

        // and: a board
        val board = variant.initialBoard()

        // when: a player tries to make a move the board
        val firstValidSquare = Square(0, 0)
        val boardTurn = board.turn
        requireNotNull(boardTurn) { "Board turn cannot be null" }
        val startingPlayer = boardTurn.player
        val secondPlayer = boardTurn.other().player
        val validMoveResult = variant.isMoveValid(board, startingPlayer, firstValidSquare)

        // then: the move is valid
        when (validMoveResult) {
            is Success -> assertIs<BoardRun>(validMoveResult.value)
            is Failure -> fail("Unexpected failure: $validMoveResult")
        }

        // and: the move is on the board
        val updatedMoves = validMoveResult.value.grid
        assertTrue(updatedMoves.size == 1)
        assertTrue(firstValidSquare in updatedMoves)

        // when: the same player tries to make another move
        val boardAfterFirstMove = validMoveResult.value
        val secondValidSquare = Square(0, 1)
        val anotherValidMoveResult = variant.isMoveValid(
            board = boardAfterFirstMove,
            player = startingPlayer,
            toSquare = secondValidSquare
        )

        // then: the move is not valid, because it's not its turn
        when (anotherValidMoveResult) {
            is Success -> fail("Unexpected success: $anotherValidMoveResult")
            is Failure -> assertIs<MakeMoveError.NotYourTurn>(anotherValidMoveResult.value)
        }

        // when: the other player tries to make a move to the previous square
        val otherValidMoveResult = variant.isMoveValid(
            board = boardAfterFirstMove,
            player = secondPlayer,
            firstValidSquare
        )

        // then: the move is not valid, since the square is already taken
        when (otherValidMoveResult) {
            is Success -> fail("Unexpected success: $otherValidMoveResult")
            is Failure -> assertIs<MakeMoveError.PositionTaken>(otherValidMoveResult.value)
        }

        // when: the other player tries to make a move to a valid square
        val secondValidMoveResult = variant.isMoveValid(
            board = boardAfterFirstMove,
            player = secondPlayer,
            toSquare = secondValidSquare
        )

        // then: the move is valid
        when (secondValidMoveResult) {
            is Success -> assertIs<BoardRun>(secondValidMoveResult.value)
            is Failure -> fail("Unexpected failure: $secondValidMoveResult")
        }

        // when: the other player tries to make a move to an invalid square in this board size
        val invalidSquare = Square(Column.MAX_INDEX, Column.MAX_INDEX)
        val invalidMoveResult = variant.isMoveValid(
            board = boardAfterFirstMove,
            player = startingPlayer,
            toSquare = invalidSquare
        )

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.InvalidPosition>(invalidMoveResult.value)
        }
    }

    @Test
    override fun `can detect a diagonal slash win`() {
        // given: a variant
        val variant = FreestyleVariant

        // when: two players make moves
        val moves = listOf(
            Square(0, 0),
            Square(0, 1),
            Square(1, 1),
            Square(0, 2),
            Square(2, 2),
            Square(0, 3),
            Square(3, 3),
            Square(0, 4),
            Square(4, 4)
        ).reversed()
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)

        // when: the player tries to make another move
        val invalidMoveResult = variant.isMoveValid(
            board = boardAfterMoves,
            player = Player.W,
            toSquare = Square(0, 5)
        )

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.GameOver>(invalidMoveResult.value)
        }
    }

    @Test
    override fun `can detect a diagonal backslash win`() {
        // given: a variant
        val variant = FreestyleVariant

        // when: two players make moves
        val moves = listOf(
            Square(0, 0),
            Square(0, 1),
            Square(1, 1),
            Square(0, 2),
            Square(2, 2),
            Square(0, 3),
            Square(3, 3),
            Square(0, 4),
            Square(4, 4)
        )
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)

        // when: the player tries to make another move
        val invalidMoveResult = variant.isMoveValid(
            board = boardAfterMoves,
            player = Player.W,
            toSquare = Square(0, 5)
        )

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.GameOver>(invalidMoveResult.value)
        }
    }

    @Test
    override fun `can detect a horizontal win`() {
        // given: a variant
        val variant = FreestyleVariant

        // when: two players make moves
        val moves = listOf(
            Square(0, 0),
            Square(1, 0),
            Square(0, 1),
            Square(1, 1),
            Square(0, 2),
            Square(1, 2),
            Square(0, 3),
            Square(1, 3),
            Square(0, 4)
        )
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)

        // when: the player tries to make another move
        val invalidMoveResult = variant.isMoveValid(
            board = boardAfterMoves,
            player = Player.W,
            toSquare = Square(0, 5)
        )

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.GameOver>(invalidMoveResult.value)
        }
    }

    @Test
    override fun `can detect a vertical win`() {
        // given: a variant
        val variant = FreestyleVariant

        // when: two players make moves
        val moves = listOf(
            Square(0, 0),
            Square(0, 1),
            Square(1, 0),
            Square(1, 1),
            Square(2, 0),
            Square(2, 1),
            Square(3, 0),
            Square(3, 1),
            Square(4, 0)
        )
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)

        // when: the player tries to make another move
        val invalidMoveResult = variant.isMoveValid(
            board = boardAfterMoves,
            player = Player.W,
            toSquare = Square(0, 6)
        )

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.GameOver>(invalidMoveResult.value)
        }
    }

    @Test
    override fun `can detect a draw`() {
        // given: a variant
        val variant = FreestyleVariant

        // when: two players make moves
        val allSquaresInBoard = possibleSquaresIn(variant.config.boardSize.size)
        println(allSquaresInBoard)
        val boardAfterMoves = makeMoves(variant, allSquaresInBoard)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardDraw>(boardAfterMoves)

        // when: the player tries to make another move
        val invalidMoveResult = variant.isMoveValid(
            board = boardAfterMoves,
            player = Player.W,
            toSquare = Square(0, 6)
        )

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.GameOver>(invalidMoveResult.value)
        }
    }
}
