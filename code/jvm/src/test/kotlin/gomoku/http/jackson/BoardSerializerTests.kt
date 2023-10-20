package gomoku.http.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BoardSerializerTests {

    companion object {
        val boardToTest = BoardRun(
            size = BoardSize.FIFTEEN,
            moves = mapOf(
                Move(Square(Column('c'), Row(9)), Piece(Player.w)),
                Move(Square(Column('d'), Row(8)), Piece(Player.b)),
                Move(Square(Column('a'), Row(6)), Piece(Player.w)),
                Move(Square(Column('b'), Row(7)), Piece(Player.b)),
                Move(Square(Column('b'), Row(11)), Piece(Player.b))
            ),
            turn = BoardTurn(Player.b, 28),
            timeLeftInSec = 28
        )
        val expectedJsonString = """
            {"grid":["c9-w","d8-b","a6-w","b7-b","b11-b"],"turn":{"player":"b","timeLeftInSec":28}}
        """.trimIndent()
    }

    @Test
    fun `Serialization process works as expected`() {
        // given: a jackson ObjectMapper and a Board instance
        val objectMapper = ObjectMapper()

        // when: the Board is serialized to a JSON string
        val jsonString = objectMapper.writeValueAsString(boardToTest)

        // then: the JSON string matches the expected value
        assertEquals(expectedJsonString, jsonString)
    }

    @Test
    fun `Deserialization process works as expected`() {
        // given: a jackson ObjectMapper and a JSON string
        val objectMapper = ObjectMapper()

        // when: the JSON string is deserialized to a Board instance
        val board = objectMapper.readValue(expectedJsonString, Board::class.java)

        // then: the Board instance matches the expected value
        assertEquals(boardToTest, board)
    }
}
