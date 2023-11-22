package gomoku.domain.variant

import gomoku.domain.components.PositiveValue
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.isFinished
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.variant.OmokVariant
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.get
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class OmokVariantTest : VariantTest() {

    override val variant = OmokVariant

    private val movesThatValidateProRule = listOf(
        getCenterSquares(variant.config.boardSize.size).first().also {
            println("Center square: $it")
        },
        Square("a1"),
        getCenterSquares(variant.config.boardSize.size).first().let {
            Square(
                it.col.toIndex() - 3,
                it.row.toIndex() - 3
            ).also { sqr ->
                println("Square in 3 intersections apart from center square: $sqr")
            }
        }
    ).also { println("Moves that validate Pro rule: $it") }

    @Test
    override fun `can make moves on the board`() {
        // given: a board
        val board = variant.initialBoard()

        // and: two players
        val boardTurn = board.turn
        requireNotNull(boardTurn) { "Board turn cannot be null" }
        val startingPlayer = boardTurn.player
        val secondPlayer = boardTurn.other().player

        // when: a player tries to make a move the board
        val firstSquare = Square(0, 0)
        val invalidProRuleMoveResult = variant.isMoveValid(board, startingPlayer, firstSquare)

        // then: the move is not valid, because it's not respecting the Pro opening rule first placement
        when (invalidProRuleMoveResult) {
            is Success -> fail("Unexpected success: $invalidProRuleMoveResult")
            is Failure -> assertIs<MakeMoveError.InvalidPosition>(invalidProRuleMoveResult.value)
        }

        // when: the player tries to make a move respecting the Pro opening rule
        val firstValidSquare = getCenterSquares(variant.config.boardSize.size).first()
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

        // then: the move is not valid, because it's not its turn
        val anotherValidMoveResult = variant.isMoveValid(boardAfterFirstMove, startingPlayer, secondValidSquare)
        when (anotherValidMoveResult) {
            is Success -> fail("Unexpected success: $anotherValidMoveResult")
            is Failure -> assertIs<MakeMoveError.NotYourTurn>(anotherValidMoveResult.value)
        }

        // when: the other player tries to make a move to the previous square
        val otherValidMoveResult = variant.isMoveValid(boardAfterFirstMove, secondPlayer, firstValidSquare)

        // then: the move is not valid, since the square is already taken
        when (otherValidMoveResult) {
            is Success -> fail("Unexpected success: $otherValidMoveResult")
            is Failure -> assertIs<MakeMoveError.PositionTaken>(otherValidMoveResult.value)
        }

        // when: the other player tries to make a move to a valid square (can be any square)
        val secondValidMoveResult = variant.isMoveValid(boardAfterFirstMove, secondPlayer, secondValidSquare)

        // then: the move is valid
        when (secondValidMoveResult) {
            is Success -> assertIs<BoardRun>(secondValidMoveResult.value)
            is Failure -> fail("Unexpected failure: $secondValidMoveResult")
        }

        // when: the other player tries to make a move to an invalid square in this board size
        val invalidSquare = Square(Column.MAX_INDEX, Column.MAX_INDEX)
        val invalidMoveResult = variant.isMoveValid(boardAfterFirstMove, startingPlayer, invalidSquare)

        // then: the move is not valid
        when (invalidMoveResult) {
            is Success -> fail("Unexpected success: $invalidMoveResult")
            is Failure -> assertIs<MakeMoveError.InvalidPosition>(invalidMoveResult.value)
        }
    }

    @Test
    fun `pro opening rule is respected`() {
        // given: a board
        val board = variant.initialBoard()

        // and: two players
        val boardTurn = board.turn
        requireNotNull(boardTurn) { "Board turn cannot be null" }
        val startingPlayer = boardTurn.player
        val secondPlayer = boardTurn.other().player

        // when: a player tries to make a move the board
        val invalidProRule1stSquare = Square(0, 0)
        val invalidProRule1stMoveResult = variant.isMoveValid(board, startingPlayer, invalidProRule1stSquare)

        // then: the move is not valid, because it's not respecting the Pro opening rule first placement
        when (invalidProRule1stMoveResult) {
            is Success -> fail("Unexpected success: $invalidProRule1stMoveResult")
            is Failure -> assertIs<MakeMoveError.InvalidPosition>(invalidProRule1stMoveResult.value)
        }

        // when: the player tries to make a move respecting the Pro opening rule
        val validProRule1stSquare = getCenterSquares(variant.config.boardSize.size).first()
        val validProRule1stMoveResult = variant.isMoveValid(
            board = board,
            player = startingPlayer,
            toSquare = validProRule1stSquare
        )

        // then: the move is valid
        when (validProRule1stMoveResult) {
            is Success -> assertIs<BoardRun>(validProRule1stMoveResult.value)
            is Failure -> fail("Unexpected failure: $validProRule1stMoveResult")
        }

        // and: the move is on the board
        val updatedMoves = validProRule1stMoveResult.value.grid
        assertTrue(updatedMoves.size == 1)
        assertTrue(validProRule1stSquare in updatedMoves)

        // when: the other player makes a move (can be any square)
        val secondValidSquare = Square(0, 1)
        val secondValidMoveResult = variant.isMoveValid(
            board = validProRule1stMoveResult.value,
            player = secondPlayer,
            toSquare = secondValidSquare
        )

        // then: the move is valid
        when (secondValidMoveResult) {
            is Success -> assertIs<BoardRun>(secondValidMoveResult.value)
            is Failure -> fail("Unexpected failure: $secondValidMoveResult")
        }

        // and: the move is on the board
        val updatedMovesAfterSecondMove = secondValidMoveResult.value.grid
        assertTrue(updatedMovesAfterSecondMove.size == 2)
        assertTrue(secondValidSquare in updatedMovesAfterSecondMove)

        // when: the starting player tries to make another move
        val boardAfterSecondMove = secondValidMoveResult.value
        val invalidProRule2ndSquare = Square(0, 1)
        val invalidProRule2ndMoveResult = variant.isMoveValid(
            board = boardAfterSecondMove,
            player = startingPlayer,
            toSquare = invalidProRule2ndSquare
        )

        // then: the move is not valid, because it's not respecting the Pro opening rule second placement
        when (invalidProRule2ndMoveResult) {
            is Success -> fail("Unexpected success: $invalidProRule2ndMoveResult")
            is Failure -> assertIs<MakeMoveError.InvalidPosition>(invalidProRule2ndMoveResult.value)
        }

        // when: the starting player tries to make a move respecting the Pro opening rule
        val validProRule2ndSquare = getCenterSquares(variant.config.boardSize.size).first()
        val nrOfIntersections = 3
        val squareIn3Intersections =
            Square(
                validProRule2ndSquare.col.toIndex() - nrOfIntersections,
                validProRule2ndSquare.row.toIndex() - nrOfIntersections
            )
        assertTrue(
            squareIn3Intersections.isNIntersectionsApartFrom(
                validProRule2ndSquare,
                PositiveValue(nrOfIntersections).get()
            )
        )
        val validProRule2ndMoveResult =
            variant.isMoveValid(
                board = boardAfterSecondMove,
                player = startingPlayer,
                toSquare = squareIn3Intersections
            )

        // then: the move is valid
        when (validProRule2ndMoveResult) {
            is Success -> assertIs<BoardRun>(validProRule2ndMoveResult.value)
            is Failure -> fail("Unexpected failure: ${validProRule2ndMoveResult.value}")
        }

        // and: the move is on the board
        val updatedMovesAfterThirdMove = validProRule2ndMoveResult.value.grid
        assertTrue(updatedMovesAfterThirdMove.size == 3)
        assertTrue(validProRule2ndSquare in updatedMovesAfterThirdMove)
    }

    @Test
    override fun `can detect a diagonal slash win`() {
        // given: two players make moves
        val moves = movesThatValidateProRule + listOf(
            Square("a2"),
            Square("h8"),
            Square("a3"),
            Square("g7"),
            Square("a4"),
            Square("e5")
        )

        // when: the board is updated with the moves
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)
    }

    @Test
    override fun `can detect a diagonal backslash win`() {
        // given: two players make moves
        val moves = movesThatValidateProRule + listOf(
            Square("a2"),
            Square("j8"),
            Square("a3"),
            Square("k7"),
            Square("a4"),
            Square("l6"),
            Square("b1"),
            Square("m5")
        )

        // when: the board is updated with the moves
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)
    }

    @Test
    override fun `can detect a horizontal win`() {
        // given: two players make moves
        val moves = movesThatValidateProRule + listOf(
            Square("b1"),
            Square("a2"),
            Square("c1"),
            Square("f3"),
            Square("d1"),
            Square("e5"),
            Square("e1")
        )

        // when: the board is updated with the moves
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)
    }

    @Test
    override fun `can detect a vertical win`() {
        // given: two players make moves
        val moves = movesThatValidateProRule + listOf(
            Square("a2"),
            Square("b1"),
            Square("a3"),
            Square("c1"),
            Square("a4"),
            Square("d1"),
            Square("a5")
        )

        // when: the board is updated with the moves
        val boardAfterMoves = makeMoves(variant, moves)

        // then: the game is finished
        assertTrue(boardAfterMoves.isFinished())
        assertIs<BoardWin>(boardAfterMoves)
    }

    @Test
    override fun `can detect a draw`() {
        // when: two players make moves
        val drawSquareSequence = getDrawSquareSequence(variant).toMutableList()
            .also { println("Before: draw square sequence: $it") }
        val firstSquare = drawSquareSequence[0]
        val thirdSquare = drawSquareSequence[2]
        val firstProRuleSquare = movesThatValidateProRule[0]
        val thirdProRuleSquare = movesThatValidateProRule[2]
        val index = drawSquareSequence.indexOf(firstProRuleSquare)
        drawSquareSequence[index] = firstSquare
        drawSquareSequence[0] = firstProRuleSquare

        val index2 = drawSquareSequence.indexOf(thirdProRuleSquare)
        drawSquareSequence[index2] = thirdSquare
        drawSquareSequence[2] = thirdProRuleSquare

        println("After: draw square sequence: $drawSquareSequence")
        val boardAfterMoves = makeMoves(variant, drawSquareSequence)

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
