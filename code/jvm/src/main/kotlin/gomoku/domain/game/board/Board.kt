package gomoku.domain.game.board

import gomoku.domain.game.board.moves.Moves
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.errors.BoardMakeMoveResult
import gomoku.domain.game.variant.Variant

sealed class Board(val grid: Moves, val turn: BoardTurn?)
class BoardRun(moves: Moves, turn: BoardTurn) : Board(moves, turn)
class BoardWin(moves: Moves, val winner: Player) : Board(moves, null)
class BoardDraw(moves: Moves) : Board(moves, null)

fun Board.play(variant: Variant, square: Square): BoardMakeMoveResult =
    variant.isMoveValid(this, square)
fun Board.isFinished(): Boolean = this is BoardWin || this is BoardDraw
