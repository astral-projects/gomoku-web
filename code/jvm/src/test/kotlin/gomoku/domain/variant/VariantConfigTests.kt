package gomoku.domain.variant

import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.domain.game.variant.config.VariantName
import gomoku.domain.variant.VariantTest.Companion.getCenterSquares
import gomoku.domain.variant.VariantTest.Companion.maximumBoardSizeSquares
import gomoku.domain.variant.VariantTest.Companion.possibleSquaresIn
import gomoku.domain.variant.VariantTest.Companion.possibleSquaresOutside
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VariantConfigTests {

    @Test
    fun `should check correctly if a square is in the center of an even board`() {
        // given: an even size
        val size = 6

        // when: retrieving all center squares of the board
        val centerSquares = getCenterSquares(size)

        // then: the center squares should be the ones expected
        assertEquals(
            listOf(
                Square("c3")
            ),
            centerSquares
        )
    }

    @Test
    fun `should check correctly if a square is in the center of an odd board`() {
        // given: an odd size
        val size = 5

        // when: retrieving all center squares of the board
        val centerSquares = getCenterSquares(size)

        // then: the center squares should be the ones expected
        assertEquals(
            listOf(
                Square("b2"),
                Square("b3"),
                Square("c2"),
                Square("c3")
            ),
            centerSquares
        )
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("provideVariantConfigArguments")
    fun `should check correctly if a square is in bounds of given board size`(
        name: String,
        variantConfig: VariantConfig,
    ) {
        // given: a variant config
        // when: checking if a square is in bounds of the board
        val square = Square("a1")

        // then: the square should be in bounds of the board
        assertTrue(variantConfig.isSquareInBounds(square))

        // when: using a list of all squares in the board
        val allInSquares = possibleSquaresIn(variantConfig.boardSize.size)

        // then: then all squares should be in bounds of the board
        allInSquares.forEach {
            assertTrue(variantConfig.isSquareInBounds(it))
        }

        // when: using a list of all squares outside the board
        val allOutsideSquares = possibleSquaresOutside(variantConfig.boardSize.size)

        // then: then all squares should not be in bounds of the board
        allOutsideSquares.forEach {
            assertFalse(variantConfig.isSquareInBounds(it))
        }

        // when: the in list is converted to a set
        val allInSquaresSet = allInSquares.toSet()

        // then: the set should have the same size as the list
        assertEquals(allInSquaresSet.size, allInSquares.size)

        // and: the intersection of the two sets (in and out of a board) should be the empty
        assertTrue(allInSquares.intersect(allOutsideSquares.toSet()).isEmpty())

        // when: the out and total lists are converted to sets
        val allOutsideSquaresSet = allOutsideSquares.toSet()
        val maximumBoardSizeSquaresSet = maximumBoardSizeSquares().toSet()

        // then: the sets should have the same size as the lists
        assertEquals(allInSquaresSet.size, allInSquares.size)
        assertEquals(maximumBoardSizeSquaresSet.size, maximumBoardSizeSquares().size)

        // when: the union of the two sets (in and out of a board) is calculated
        val union = allInSquaresSet.union(allOutsideSquaresSet)

        // then: the union should have the same size as the maximum board size
        assertEquals(union.size, maximumBoardSizeSquaresSet.size)

        // and: the union should be equal to the maximum board size
        assertEquals(allInSquaresSet.union(allOutsideSquaresSet), maximumBoardSizeSquaresSet)
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("provideVariantConfigArguments")
    fun `should check correctly if a square is in the board center(s) intersection(s)`(
        name: String,
        variantConfig: VariantConfig,
    ) {
        // given: a variant config
        // when: checking if a square is in the center of the board
        val square = Square("a1") // unless the board size is 1

        // then: the square should not be in the center of the board
        assertFalse(variantConfig.isSquareInCenter(square))

        // when: retrieving all center squares
        val centerSquares = getCenterSquares(variantConfig.boardSize.size)

        // then: the center squares should be in the center of the board
        centerSquares.forEach {
            assertTrue(variantConfig.isSquareInCenter(it))
        }

        // when: all squares in the board that are not in the center are retrieved
        val allInSquares = possibleSquaresIn(variantConfig.boardSize.size)

        // then: the set conversion of the list should have the same size as the list
        val allInSquaresSet = allInSquares.toSet()
        assertEquals(allInSquaresSet.size, allInSquares.size)

        // when: the center squares are converted to a set
        val centerSquaresSet = centerSquares.toSet()

        // then: the set conversion of the list should have the same size as the list
        assertEquals(centerSquaresSet.size, centerSquares.size)

        // and: the center squares should be in the board
        centerSquares.forEach {
            assertTrue(allInSquaresSet.contains(it))
        }

        // when: the center squares are subtracted from the squares in the board
        val notInCenterSquares: Set<Square> = allInSquaresSet.minus(centerSquaresSet)

        // then: the set should have the same size as all squares in the board minus the center squares
        assertEquals(notInCenterSquares.size, allInSquares.size - centerSquares.size)

        // when: the squares supposedly not in the center are checked
        // then: they should not be in the center
        notInCenterSquares.forEach {
            assertFalse(variantConfig.isSquareInCenter(it))
        }
    }

    companion object {

        @JvmStatic
        fun provideVariantConfigArguments(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "Board size is even",
                    VariantConfig(
                        name = VariantName.FREESTYLE,
                        openingRule = OpeningRule.PRO,
                        boardSize = BoardSize.SIX
                    )
                ),
                Arguments.of(
                    "Board size is odd",
                    VariantConfig(
                        name = VariantName.FREESTYLE,
                        openingRule = OpeningRule.PRO,
                        boardSize = BoardSize.FIVE
                    )
                )
            )
    }
}
