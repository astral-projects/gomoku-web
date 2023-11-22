package gomoku.domain.game.variant

import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PositiveValue
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
private const val WINNER_POINTS = 400
private const val LOSER_POINTS = 200
private const val DRAW_POINTS = 250
private const val WINNER_ON_FORFEIT_OR_TIMER_POINTS = 500
private const val FORFEITER_POINTS = 50
private const val MAX_TURN_TIME_IN_SEC = 90

/**
 * Represents the Omok variant of the game.
 * Omok is similar to Freestyle gomoku, however, it is played on a 19×19 board and
 * includes the rule of three and three.
 * This rule prohibits a player from making a move that simultaneously forms two open
 * rows of three stones (rows not blocked by an opponent's stone at either end).
 * @see <a href="https://en.wikipedia.org/wiki/Gomoku#Omok">Omok Wiki</a>
 */
@Component
object OmokVariant : Variant {

    private val startingPlayer: Player = Player.B
    private val nrIntersectionsForProRule = PositiveValue(3).get()

    override val config: VariantConfig = VariantConfig(
        name = VariantName.OMOK,
        openingRule = OpeningRule.PRO,
        boardSize = BoardSize.NINETEEN
    )

    @Throws(IllegalArgumentException::class)
    override fun isMoveValid(board: Board, player: Player, toSquare: Square): BoardMakeMoveResult {
        when (board) {
            is BoardWin, is BoardDraw -> return Failure(MakeMoveError.GameOver)
            is BoardRun -> {
                requireNotNull(board.turn) { "Board turn cannot be null" }
                if (toSquare in board.grid) return Failure(MakeMoveError.PositionTaken(toSquare))
                if (board.turn.player != player) return Failure(MakeMoveError.NotYourTurn(player))
                if (!isPositionValid(board, toSquare)) return Failure(MakeMoveError.InvalidPosition(toSquare))
                val updatedMoves = board.grid + Move(toSquare, Piece(board.turn.player))
                return when {
                    checkWin(board, toSquare) -> Success(BoardWin(updatedMoves, board.turn.player))
                    checkDraw(updatedMoves) -> Success(BoardDraw(updatedMoves))
                    else -> Success(BoardRun(updatedMoves, board.turn.other()))
                }
            }
        }
    }

    override fun initialBoard(): Board = BoardRun(emptyMap(), BoardTurn(startingPlayer, turnTimer))

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

    /**
     * Checks if the position where the move is being made to is valid.
     * @param board The game board.
     * @param square The square where the move is being made to.
     * @return true if the position is invalid, false otherwise.
     */
    private fun isPositionValid(
        board: Board,
        square: Square
    ): Boolean {
        if (board.grid.isEmpty()) {
            // Check the Pro opening rule for the initial placement
            if (isProInitialPlacementValid(square)) {
                return true
            }
        } else if (board.grid.size == 2) {
            // Check the Pro opening rule for the second placement
            if (isProSecondPlacementValid(board, square)) {
                return true
            }
        } else if (config.isSquareInBounds(square)) {
            return true
        }
        return false
    }

    /**
     * Checks if the Pro opening rule for the inicial placement is valid.
     * The Pro opening rule states that the first move must be made in the center of the board.
     * @param square The square where the move is being made to.
     */
    private fun isProInitialPlacementValid(square: Square): Boolean =
        config.isSquareInCenter(square)

    /**
     * Checks if the Pro opening rule for the second placement is valid.
     * The Pro opening rule states that the second move must be made at least three
     * intersections apart from the first move.
     * @param board The game board.
     * @param square The square where the move is being made to.
     */
    private fun isProSecondPlacementValid(board: Board, square: Square): Boolean {
        val firstSquare = board.grid
            .filter { it.value == Piece(startingPlayer) }
            .keys.firstOrNull() ?: return false
        return firstSquare.isNIntersectionsApartFrom(square, nrIntersectionsForProRule)
    }
}
