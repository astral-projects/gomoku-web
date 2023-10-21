package gomoku.domain.game.board

import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.Variant

sealed class Board(val grid: Moves, val turn: BoardTurn?) {
    fun copy(grid: Moves = this.grid, turn: BoardTurn? = this.turn): Board = when (this) {
        is BoardRun -> BoardRun(grid, turn as BoardTurn)
        is BoardWin -> BoardWin(grid, winner)
        is BoardDraw -> BoardDraw(grid)
    }
    fun play(pos: Square, variant: Variant): Board = variant.isMoveValid(this, pos)

    fun isFinished(variant: Variant): Boolean = variant.isFinished(this)
}

class BoardRun(moves: Moves, turn: BoardTurn) : Board(moves, turn)
class BoardWin(moves: Moves, val winner: Player) : Board(moves, null)
class BoardDraw(moves: Moves) : Board(moves, null)