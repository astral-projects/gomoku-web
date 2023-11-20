package gomoku.domain

import gomoku.domain.components.PositiveValue
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
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
            Square("j10"),
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
            Square("k11"),
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
            Square("l12"),
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

}