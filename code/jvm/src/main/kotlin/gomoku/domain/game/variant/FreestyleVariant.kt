package gomoku.domain.game.variant

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.game.GamePoints
import gomoku.domain.game.GamePointsOnDraw
import gomoku.domain.game.GamePointsOnForfeitOrTimer
import gomoku.domain.game.GamePointsOnWin
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardDraw
import gomoku.domain.game.board.BoardRun
import gomoku.domain.game.board.BoardTurn
import gomoku.domain.game.board.BoardWin
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.errors.BoardMakeMoveResult
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.Move
import gomoku.domain.game.board.moves.move.Piece
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantConfig
import gomoku.domain.game.variant.config.VariantName
import gomoku.utils.Failure
import gomoku.utils.Success
import gomoku.utils.get
import org.springframework.stereotype.Component

// Constants
private const val WINNER_POINTS = 300
private const val LOSER_POINTS = 100
private const val DRAW_POINTS = 150
private const val WINNER_ON_FORFEIT_OR_TIMER_POINTS = 400
private const val FORFEITER_POINTS = 0
private const val MAX_TURN_TIME_IN_SEC = 60

/**
 * Represents the Freestyle variant of the game.
 * Freestyle gomoku has no restrictions on either player and allows a player to win by creating a line
 * of five or more stones, with each player alternating turns placing one stone at a time.
 * It is played on a 15×15 board.
 * @see <a href="https://en.wikipedia.org/wiki/Gomoku#Freestyle">Freestyle Wiki</a>
 */
@Component
object FreestyleVariant : Variant {

    private val startingPlayer = Player.W

    override val config: VariantConfig = VariantConfig(
        name = VariantName.FREESTYLE,
        openingRule = OpeningRule.NONE,
        boardSize = BoardSize.FIFTEEN
    )

    @Throws(IllegalArgumentException::class)
    override fun isMoveValid(board: Board, player: Player, toSquare: Square): BoardMakeMoveResult {
        when (board) {
            is BoardWin, is BoardDraw -> return Failure(MakeMoveError.GameOver)
            is BoardRun -> {
                requireNotNull(board.turn) { "Board turn cannot be null" }
                if (!config.isSquareInBounds(toSquare)) {
                    return Failure(MakeMoveError.InvalidPosition(toSquare))
                }
                if (toSquare in board.grid) return Failure(MakeMoveError.PositionTaken(toSquare))
                if (board.turn.player != player) return Failure(MakeMoveError.NotYourTurn(player))
                val updatedMoves = board.grid + Move(toSquare, Piece(board.turn.player))
                return when {
                    checkWin(board, toSquare) -> Success(BoardWin(updatedMoves, board.turn.player))
                    checkDraw(updatedMoves) -> Success(BoardDraw(updatedMoves))
                    else -> Success(BoardRun(updatedMoves, board.turn.other()))
                }
            }
        }
    }

    override fun initialBoard(): BoardRun = BoardRun(emptyMap(), BoardTurn(startingPlayer, turnTimer))

    override val points: GamePoints
        get() = GamePoints(
            onFinish = GamePointsOnWin(
                winner = NonNegativeValue(WINNER_POINTS).get(),
                loser = NonNegativeValue(LOSER_POINTS).get()
            ),
            onDraw = GamePointsOnDraw(
                shared = NonNegativeValue(DRAW_POINTS).get()
            ),
            onForfeitOrTimer = GamePointsOnForfeitOrTimer(
                winner = NonNegativeValue(WINNER_ON_FORFEIT_OR_TIMER_POINTS).get(),
                forfeiter = NonNegativeValue(FORFEITER_POINTS).get()
            )
        )

    override val turnTimer: NonNegativeValue
        get() = NonNegativeValue(MAX_TURN_TIME_IN_SEC).get()
}
