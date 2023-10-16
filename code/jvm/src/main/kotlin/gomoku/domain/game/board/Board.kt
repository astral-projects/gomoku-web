package gomoku.domain.game.board

import gomoku.domain.SerializableDomainModel
import gomoku.domain.game.board.moves.Moves

class Board(
    val grid: Moves,
    val turn: BoardTurn
) : SerializableDomainModel