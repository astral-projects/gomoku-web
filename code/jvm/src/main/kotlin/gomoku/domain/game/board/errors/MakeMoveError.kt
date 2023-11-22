package gomoku.domain.game.board.errors

import gomoku.domain.game.board.Board
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.utils.Either

sealed class MakeMoveError {
    data object GameOver : MakeMoveError()
    class NotYourTurn(val player: Player) : MakeMoveError()
    class PositionTaken(val square: Square) : MakeMoveError()
    class InvalidPosition(val square: Square) : MakeMoveError()
}

typealias BoardMakeMoveResult = Either<MakeMoveError, Board>
