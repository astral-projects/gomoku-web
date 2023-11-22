package gomoku.domain

import gomoku.domain.components.PositiveValue
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.variant.VariantTest.Companion.maximumBoardSizeSquares
import gomoku.utils.get
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SquareTest {

    @Test
    fun `square is constructed correctly`() {
        // given: a square
        val square = Square("a1")

        // then: the square should be constructed correctly
        assertTrue(square.col == Column('a').get())
        assertTrue(square.col.hashCode() == Column('a').get().hashCode())
        assertTrue(square.row == Row(0).get())
        assertTrue(square.row.hashCode() == Row(0).get().hashCode())
    }

    @Test
    fun `square is converted to string correctly`() {
        // given: a square string representationa
        val squareString = "a1"

        // when: the square is constructed from the string
        val square = Square(squareString)

        // then: the square should be converted to string correctly
        assertTrue(square.toString() == "a1")

        // given: a square string representationa
        val squareStringB = "b2"

        // when: the square is constructed from the string
        val squareB = Square(squareStringB)

        // then: the square should be converted to string correctly
        assertTrue(squareB.toString() == "b2")
    }

    @Test
    fun `square is converted to index correctly`() {
        // given: a square
        val square = Square("a1")

        // then: the square should be converted to index correctly
        assertTrue(square.col.toIndex() == 0)
        assertTrue(square.row.toIndex() == 0)

        // given: a square
        val square2 = Square("b2")

        // then: the square should be converted to index correctly
        assertTrue(square2.col.toIndex() == 1)
        assertTrue(square2.row.toIndex() == 1)
    }

    @Test
    fun `throws exception when square is constructed with strings that represent invalid columns and-or rows`() {
        assertThrows<IllegalArgumentException> {
            Square("A1")
        }
        assertThrows<IllegalArgumentException> {
            Square("a0")
        }
        assertThrows<IllegalArgumentException> {
            Square("a-1")
        }
        assertThrows<IllegalArgumentException> {
            Square("a")
        }
        assertThrows<IllegalArgumentException> {
            Square("1")
        }
    }

    @Test
    fun `should return true when the squares are N intersections apart`() {
        // given: a square placed closed to the center of the board
        val square = Square("i9")

        // and: a list of squares that are one intersection apart from the square
        val adjacentSquares = listOf(
            Square("h8"),
            Square("h9"),
            Square("h10"),
            Square("i8"),
            Square("i10"),
            Square("j8"),
            Square("j9"),
            Square("j10")
        )

        // then: all of them should be one intersection apart from the square
        val oneIntersection = PositiveValue(1).get()
        adjacentSquares.forEach {
            println(it)
            assertTrue(square.isNIntersectionsApartFrom(it, oneIntersection))
        }

        // when: a list of squares that are not one intersection apart from the square
        val twoIntersectionsApart = listOf(
            Square("g7"),
            Square("g8"),
            Square("g9"),
            Square("g10"),
            Square("g11"),
            Square("h7"),
            Square("h11"),
            Square("i7"),
            Square("i11"),
            Square("j7"),
            Square("j11"),
            Square("k7"),
            Square("k8"),
            Square("k9"),
            Square("k10"),
            Square("k11")
        )

        // then: none of them should be one intersection apart from the square
        twoIntersectionsApart.forEach {
            assertFalse(square.isNIntersectionsApartFrom(it, oneIntersection))
        }

        // when: a list of squares that are two intersections apart from the square
        val threeIntersectionsApart = listOf(
            Square("f6"),
            Square("f7"),
            Square("f8"),
            Square("f9"),
            Square("f10"),
            Square("f11"),
            Square("f12"),
            Square("g6"),
            Square("g12"),
            Square("h6"),
            Square("h12"),
            Square("i6"),
            Square("i12"),
            Square("j6"),
            Square("j12"),
            Square("k6"),
            Square("k12"),
            Square("l6"),
            Square("l7"),
            Square("l8"),
            Square("l9"),
            Square("l10"),
            Square("l11"),
            Square("l12")
        )

        // then: all of them should be two intersections apart from the square
        val twoIntersections = PositiveValue(2).get()
        twoIntersectionsApart.forEach {
            assertTrue(square.isNIntersectionsApartFrom(it, twoIntersections))
        }

        // when: a list of squares that are not two intersections apart from the square
        // then: none of them should be two intersections apart from the square
        threeIntersectionsApart.forEach {
            assertFalse(square.isNIntersectionsApartFrom(it, twoIntersections))
        }
        adjacentSquares.forEach {
            assertFalse(square.isNIntersectionsApartFrom(it, twoIntersections))
        }

        // when: a list of squares that are three intersections apart from the square
        val threeIntersections = PositiveValue(3).get()
        // then: all of them should be three intersections apart from the square
        threeIntersectionsApart.forEach {
            assertTrue(square.isNIntersectionsApartFrom(it, threeIntersections))
        }

        // when: a list of squares that are not three intersections apart from the square
        // then: none of them should be three intersections apart from the square
        adjacentSquares.forEach {
            assertFalse(square.isNIntersectionsApartFrom(it, threeIntersections))
        }
        twoIntersectionsApart.forEach {
            assertFalse(square.isNIntersectionsApartFrom(it, threeIntersections))
        }
    }

    @Test
    fun `can detect consecutive squares`() {
        // given: a square that is not in a corner
        val square = Square("e5")

        // and: a list of squares that are consecutive to the square
        val consecutiveSquares = listOf(
            Square("d4"),
            Square("d5"),
            Square("d6"),
            Square("e4"),
            Square("e6"),
            Square("f4"),
            Square("f5"),
            Square("f6")
        )

        // then: all of them should be consecutive to the square
        consecutiveSquares.forEach {
            assertTrue(square.isConsecutiveTo(it))
        }

        // when: a list of squares that are not consecutive to the square
        val nonConsecutiveSquares = maximumBoardSizeSquares().minus(consecutiveSquares.toSet())

        // then: none of them should be consecutive to the square
        nonConsecutiveSquares.forEach {
            assertFalse(square.isConsecutiveTo(it))
        }

        // given: the left top corner square
        val leftTopCornerSquare = Square("a1")

        // and: a list of squares that are consecutive to the square
        val leftTopCornerConsecutiveSquares = listOf(
            Square("a2"),
            Square("b1"),
            Square("b2")
        )

        // then: all of them should be consecutive to the square
        leftTopCornerConsecutiveSquares.forEach {
            assertTrue(leftTopCornerSquare.isConsecutiveTo(it))
        }

        // when: a list of squares that are not consecutive to the square
        val nonConsecutiveSquaresOfLTSquare = maximumBoardSizeSquares().minus(leftTopCornerConsecutiveSquares.toSet())

        // then: none of them should be consecutive to the square
        nonConsecutiveSquaresOfLTSquare.forEach {
            assertFalse(leftTopCornerSquare.isConsecutiveTo(it))
        }

        // given: the right top corner square
        val rightTopCornerSquare = Square(colIndex = Column.MAX_INDEX, rowIndex = 0)

        // and: a list of squares that are consecutive to the square
        val rightTopCornerConsecutiveSquares = listOf(
            Square(colIndex = Column.MAX_INDEX - 1, rowIndex = 0),
            Square(colIndex = Column.MAX_INDEX - 1, rowIndex = 1),
            Square(colIndex = Column.MAX_INDEX, rowIndex = 1)
        )

        // then: all of them should be consecutive to the square
        rightTopCornerConsecutiveSquares.forEach {
            assertTrue(rightTopCornerSquare.isConsecutiveTo(it))
        }

        // when: a list of squares that are not consecutive to the square
        val nonConsecutiveSquaresOfRTSquare = maximumBoardSizeSquares().minus(rightTopCornerConsecutiveSquares.toSet())

        // then: none of them should be consecutive to the square
        nonConsecutiveSquaresOfRTSquare.forEach {
            assertFalse(rightTopCornerSquare.isConsecutiveTo(it))
        }

        // given: the left bottom corner square
        val leftBottomCornerSquare = Square(0, Column.MAX_INDEX)

        // and: a list of squares that are consecutive to the square
        val leftBottomCornerConsecutiveSquares = listOf(
            Square(0, Column.MAX_INDEX - 1),
            Square(1, Column.MAX_INDEX - 1),
            Square(1, Column.MAX_INDEX)
        )

        // then: all of them should be consecutive to the square
        leftBottomCornerConsecutiveSquares.forEach {
            assertTrue(leftBottomCornerSquare.isConsecutiveTo(it))
        }

        // when: a list of squares that are not consecutive to the square
        val nonConsecutiveSquaresOfLBSquare =
            maximumBoardSizeSquares().minus(leftBottomCornerConsecutiveSquares.toSet())

        // then: none of them should be consecutive to the square
        nonConsecutiveSquaresOfLBSquare.forEach {
            assertFalse(leftBottomCornerSquare.isConsecutiveTo(it))
        }

        // given: the right bottom corner square
        val rightBottomCornerSquare = Square(Column.MAX_INDEX, Column.MAX_INDEX)

        // and: a list of squares that are consecutive to the square
        val rightBottomCornerConsecutiveSquares = listOf(
            Square(Column.MAX_INDEX - 1, Column.MAX_INDEX),
            Square(Column.MAX_INDEX - 1, Column.MAX_INDEX - 1),
            Square(Column.MAX_INDEX, Column.MAX_INDEX - 1)
        )

        // then: all of them should be consecutive to the square
        rightBottomCornerConsecutiveSquares.forEach {
            assertTrue(rightBottomCornerSquare.isConsecutiveTo(it))
        }

        // when: a list of squares that are not consecutive to the square
        val nonConsecutiveSquaresOfRBSquare =
            maximumBoardSizeSquares().minus(rightBottomCornerConsecutiveSquares.toSet())

        // then: none of them should be consecutive to the square
        nonConsecutiveSquaresOfRBSquare.forEach {
            assertFalse(rightBottomCornerSquare.isConsecutiveTo(it))
        }
    }

    @Test
    fun `can detect two squares in the same row`() {
        // given: a square
        val square = Square("e5")

        // and: a list of squares that are in the same row as the square
        val squaresInSameRow = maximumBoardSizeSquares().filter {
            it.row.toIndex() == square.row.toIndex()
        }

        // then: all of them should be in the same row as the square
        squaresInSameRow.forEach {
            assertTrue(square.isInSameRow(it))
        }

        // when: a list of squares that are not in the same row as the square
        val squaresNotInSameRow = maximumBoardSizeSquares().minus(squaresInSameRow.toSet())

        // then: none of them should be in the same row as the square
        squaresNotInSameRow.forEach {
            assertFalse(square.isInSameRow(it))
        }
    }

    @Test
    fun `can detect two squares in the same column`() {
        // given: a square
        val square = Square("e5")

        // and: a list of squares that are in the same column as the square
        val squaresInSameColumn = maximumBoardSizeSquares().filter {
            it.col.toIndex() == square.col.toIndex()
        }

        // then: all of them should be in the same column as the square
        squaresInSameColumn.forEach {
            assertTrue(square.isInSameColumn(it))
        }

        // when: a list of squares that are not in the same column as the square
        val squaresNotInSameColumn = maximumBoardSizeSquares().minus(squaresInSameColumn.toSet())

        // then: none of them should be in the same column as the square
        squaresNotInSameColumn.forEach {
            assertFalse(square.isInSameColumn(it))
        }
    }

    @Test
    fun `can detect squares in the same slash`() {
        // given: a square
        val square = Square("e5")

        // and: a list of squares that are in the same slash as the square
        val squaresInSameSlash = maximumBoardSizeSquares().filter {
            it.col.toIndex() - it.row.toIndex() == square.col.toIndex() - square.row.toIndex()
        }

        // then: all of them should be in the same slash as the square
        squaresInSameSlash.forEach {
            assertTrue(square.isInSameSlash(it))
        }

        // when: a list of squares that are not in the same slash as the square
        val squaresNotInTheSameSlash = maximumBoardSizeSquares().minus(squaresInSameSlash.toSet())

        // then: none of them should be in the same slash as the square
        squaresNotInTheSameSlash.forEach {
            assertFalse(square.isInSameSlash(it))
        }
    }

    @Test
    fun `can detect squares in the same backslash`() {
        // given: a square
        val square = Square("e5")

        // and: a list of squares that are in the same backslash as the square
        val squaresInSameBackSlash = maximumBoardSizeSquares().filter {
            it.col.toIndex() + it.row.toIndex() == square.col.toIndex() + square.row.toIndex()
        }

        // then: all of them should be in the same backslash as the square
        squaresInSameBackSlash.forEach {
            assertTrue(square.isInSameBackSlash(it))
        }

        // when: a list of squares that are not in the same backslash as the square
        val squaresNotInTheSameBackSlash = maximumBoardSizeSquares().minus(squaresInSameBackSlash.toSet())

        // then: none of them should be in the same backslash as the square
        squaresNotInTheSameBackSlash.forEach {
            assertFalse(square.isInSameBackSlash(it))
        }
    }
}
