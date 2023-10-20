package gomoku.domain.game.board

import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square

const val WINNING_PIECES = 5

// TODO: make this configurable in the UI and in the backend
private val boardSize = BoardSize.FIFTEEN

const val timeout = 60

sealed class Board(val grid: Moves, val size: BoardSize, val turn: BoardTurn?) {
    fun copy(grid: Moves = this.grid, turn: BoardTurn? = this.turn): Board = when (this) {
        is BoardRun -> BoardRun(size, grid, turn as BoardTurn, timeLeftInSec)
        is BoardWin -> BoardWin(size, grid, winner)
        is BoardDraw -> BoardDraw(size, grid)
    }
}
class BoardRun(size: BoardSize, mvs: Moves, turn: BoardTurn, val timeLeftInSec: Int) : Board(mvs, size, turn)
class BoardWin(size: BoardSize, mvs: Moves, val winner: Player) : Board(mvs, size, null)
class BoardDraw(size: BoardSize, mvs: Moves) : Board(mvs, size, null)

fun initialBoard() = BoardRun(boardSize, emptyMap(), BoardTurn(Player.w, timeout), timeout)

fun Board.play(square: Square): Board {
    return when (this) {
        is BoardWin, is BoardDraw -> error("Game is over")
        is BoardRun -> {
            require(square !in grid) { "Position taken $square" }
            require(turn != null) { "Game is running" }
            val mvs = grid + Move(square, Piece(turn.player))
            when {
                checkWin(square) -> BoardWin(boardSize, mvs, turn.player)
                mvs.size == boardSize.size * boardSize.size -> BoardDraw(boardSize, mvs)
                else -> BoardRun(boardSize, mvs, turn.other(), timeLeftInSec)
            }
        }
    }
}

fun Board.isFinished(): Boolean = when (this) {
    is BoardWin, is BoardDraw -> true
    is BoardRun -> timeLeftInSec <= 0
}

private fun BoardRun.checkWin(pos: Square): Boolean {
    // slash and backslash are the diagonals
    require(turn != null) { "Game is running" }
    val backSlash = pos.row.toIndex() == pos.col.toIndex()
    val slash = pos.row.toIndex() + pos.col.toIndex() == boardSize.size - 1
    val places = grid.filter { it.value == Piece(turn.player) }.keys + pos
    return places.count { it.col == pos.col } == WINNING_PIECES ||
        places.count { it.row == pos.row } == WINNING_PIECES ||
        places.count { backSlash } == WINNING_PIECES ||
        places.count { slash } == WINNING_PIECES ||
        turn.timeLeftInSec <= 0
}
