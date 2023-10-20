package gomoku.domain

import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.game.board.play
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBoard {

    @Test
    fun `test initial board`() {
        val sut = initialBoard()
        assertEquals(0, sut.grid.size)
        assertEquals(Player.w, sut.turn?.player)
    }

    @Test
    fun `test play`() {
        val sut = initialBoard()
        val sut2 = sut.play(Square(Column('c'), Row(9)))
        assertTrue(sut2 is BoardRun)
        assertEquals(1, sut2.grid.size)
        assertEquals(Player.b, sut2.turn?.player)
    }

    @Test
    fun `test make 2 moves`() {
        val sut = initialBoard()
        val sut2 = sut.play(Square(Column('c'), Row(9)))
        val sut3 = sut2.play(Square(Column('d'), Row(8)))
        assertTrue(sut3 is BoardRun)
        assertEquals(2, sut3.grid.size)
        assertEquals(Player.w, sut3.turn?.player)
    }

    @Test
    fun `make an invalid move`() {
        val sut = initialBoard()
        val sut2 = sut.play(Square(Column('c'), Row(9)))
        val sut3 = sut2.play(Square(Column('d'), Row(8)))
        assertTrue(sut3 is BoardRun)
        assertEquals(2, sut3.grid.size)
        assertThrows<IllegalArgumentException> { sut3.play(Square(Column('c'), Row(9))) }
    }

    @Test
    fun `make a board win`() {
        val sut = initialBoard().play(Square(Column('c'), Row(9)))
            .play(Square(Column('d'), Row(8)))
            .play(Square(Column('c'), Row(8)))
            .play(Square(Column('d'), Row(7)))
            .play(Square(Column('c'), Row(7)))
            .play(Square(Column('d'), Row(6)))
            .play(Square(Column('c'), Row(6)))
            .play(Square(Column('d'), Row(5)))
            .play(Square(Column('c'), Row(5)))

        assertTrue(sut is BoardWin)
        assertEquals(Player.w, sut.winner)
    }

    @Test
    fun `make a win because a player took to much time to play`() {
        val boardWithEndedTurn = BoardRun(
            BoardSize.FIFTEEN,
            mapOf(
                Square(Column('c'), Row(9)) to Piece(Player.w),
                Square(Column('d'), Row(8)) to Piece(Player.b)
            ),
            BoardTurn(Player.b, 0),
            60
        )
        val sut = boardWithEndedTurn.play(Square(Column('c'), Row(14)))
        assertTrue(sut is BoardWin)
        assertEquals(Player.b, sut.winner)
    }

    @Test
    fun `make a win in the diagonal`() {
        val sut = initialBoard().play(Square(Column('a'), Row(1)))
            .play(Square(Column('a'), Row(2)))
            .play(Square(Column('b'), Row(2)))
            .play(Square(Column('b'), Row(3)))
            .play(Square(Column('c'), Row(3)))
            .play(Square(Column('c'), Row(4)))
            .play(Square(Column('d'), Row(4)))
            .play(Square(Column('d'), Row(5)))
            .play(Square(Column('e'), Row(6)))
            .play(Square(Column('e'), Row(5)))

        assertTrue(sut is BoardWin)
        assertEquals(Player.b, sut.winner)
    }
}
