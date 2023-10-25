package gomoku.domain.game

import gomoku.domain.Id
import gomoku.domain.errors.MakeMoveError
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.Variant
import gomoku.domain.user.User
import gomoku.utils.Either
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.failure
import gomoku.utils.success
import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

@Component
class GameLogic(
    private val variant: Variant,
    private val clock: Clock
) {

    /**
     * Creates a new game.
     * @param id game id
     * @param gameVariant game variant
     * @param host host user
     * @param guest guest user
     * @return the new game instance.
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
            board = variant.initialBoard(),
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
    fun play(pos: Square, game: Game, userId: Id): GameMakeMoveResult {
        val board = game.board
        if (game.state != GameState.IN_PROGRESS) {
            return failure(MakeMoveError.GameOver)
        }
        if (board !is BoardRun) {
            return failure(MakeMoveError.GameOver)
        }
        if (board.turn?.player != userId.toPlayer(game) && board.turn != null) {
            return failure(MakeMoveError.NotYourTurn(board.turn.player))
        }
        return when (val newBoard = board.play(pos, variant)) {
            is Failure -> when (newBoard.value) {
                MakeMoveError.GameOver -> Failure(MakeMoveError.GameOver)
                is MakeMoveError.NotYourTurn -> Failure(MakeMoveError.NotYourTurn(newBoard.value.player))
                is MakeMoveError.PositionTaken -> Failure(MakeMoveError.PositionTaken(newBoard.value.square))
                is MakeMoveError.InvalidPosition -> Failure(MakeMoveError.InvalidPosition(newBoard.value.square))
            }
            is Success -> success(
                game.copy(
                    board = newBoard.value,
                    state = if (newBoard.value.isFinished(variant)) GameState.FINISHED else GameState.IN_PROGRESS,
                    updatedAt = clock.now()
                )
            )
        }
    }

    /**
     * Returns true if the game is over.
     *
     * @param game - game
     */
    fun isGameOver(game: Game) = when (val board = game.board) {
        is BoardWin, is BoardDraw -> true
        is BoardRun -> false
    }

    /**
     * Function that returns a player based on the user id.
     * If the user is the host, the player is white, otherwise the player is black.
     *
     * @param game - game to which the user belongs
     */
    private fun Id.toPlayer(game: Game) = if (this == game.hostId) Player.W else Player.B
}

typealias GameMakeMoveResult = Either<MakeMoveError, Game>
