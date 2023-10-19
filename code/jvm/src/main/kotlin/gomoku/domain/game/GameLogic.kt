package gomoku.domain.game

import gomoku.domain.Id
import gomoku.domain.errors.MakeMoveError
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.initialBoard
import gomoku.domain.game.board.isFinished
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.play
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.user.User
import gomoku.utils.Either
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

@Component
class GameLogic(
    private val clock: Clock
) {

    /**
     * The New game is created with empty board and initial turn.
     * @param id - game id
     * @param gameVariant - game variant
     * @param boardSize - board size
     * @param host - host user
     * @param guest - guest user
     * @return new game
     */
    fun createNewGame(
        id: Id,
        gameVariant: GameVariant,
        host: User,
        guest: User
    ): Game {
        return Game(
            id = id,
            state = GameState.IN_PROGRESS,
            variant = gameVariant,
            board = initialBoard(),
            createdAt = clock.now(),
            updatedAt = clock.now(),
            hostId = host.id,
            guestId = guest.id
        )
    }

    /**
     * Player makes a move. The move is valid if the game is in progress and the position is empty.
     *
     * @param game - game to which the user belongs
     * @param userId - user who makes a move
     * @param pos - Square position on the board
     * @return Game with new move
     */
    fun play(pos: Square, game: Game, userId: Id): MakeMoveResult {
        val board = game.board
        if (game.state != GameState.IN_PROGRESS) {
            return failure(MakeMoveError.GameIsNotInProgress)
        }
        if (board !is BoardRun) {
            return failure(MakeMoveError.GameOver)
        }
        if (board.turn?.player != userId.toPlayer(game)) {
            return failure(MakeMoveError.NotYourTurn)
        }
        val newBoard = game.board.play(pos)
        return success(
            game.copy(
                board = newBoard,
                state = if (newBoard.isFinished()) GameState.FINISHED else GameState.IN_PROGRESS,
                updatedAt = clock.now()
            )
        )
    }

    /**
     * Returns true if the game is over.
     *
     * @param game - game
     */
    fun isGameOver(game: Game) = when (val board = game.board) {
        is BoardWin, is BoardDraw -> true
        is BoardRun -> board.timeLeftInSec <= 0
    }

    /**
     * Function that returns a player based on the user id.
     * If the user is the host, the player is white, otherwise the player is black.
     *
     * @param game - game to which the user belongs
     */
    private fun Id.toPlayer(game: Game) = if (this == game.hostId) Player.w else Player.b
}

typealias MakeMoveResult = Either<MakeMoveError, Game>