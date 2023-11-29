package gomoku.http.model.game

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.Moves
import gomoku.repository.jackson.serializers.MovesSerializer

sealed class BoardOutputModel(
    @field:JsonSerialize(using = MovesSerializer::class)
    val grid: Moves
) {
    class BoardRunOutputModel(grid: Moves, val turn: BoardTurn) : BoardOutputModel(grid)
    class BoardWinOutputModel(grid: Moves, val winner: Player) : BoardOutputModel(grid)
    class BoardDrawOutputModel(grid: Moves) : BoardOutputModel(grid)

    companion object {
        fun serializeFrom(board: Board): BoardOutputModel {
            return when (board) {
                is BoardRun -> {
                    require(board.turn != null) { "BoardRun must have a turn" }
                    BoardRunOutputModel(board.grid, board.turn)
                }
                is BoardWin -> BoardWinOutputModel(board.grid, board.winner)
                is BoardDraw -> BoardDrawOutputModel(board.grid)
            }
        }
    }
}
