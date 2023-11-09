package gomoku.domain.game

import gomoku.domain.components.NonNegativeValue

/**
 * Represents the points strategies of a game.
 * @property onFinish Points that a player gets when the game is finished.
 * @property onDraw Points that a player gets when the game ends in a draw.
 * @property onForfeitOrTimer Points that a player gets when the game ends due to a forfeit or a timer.
 */
data class GamePoints(val onFinish: GamePointsOnWin, val onDraw: GamePointsOnDraw, val onForfeitOrTimer: GamePointsOnForfeitOrTimer)

/**
 * Represents the points strategies of a game on a win.
 * @property winner Points that the winner gets.
 * @property loser Points that the loser gets.
 */
data class GamePointsOnWin(val winner: NonNegativeValue, val loser: NonNegativeValue)

/**
 * Represents the points strategies of a game on a draw.
 * @property shared Points that both players get.
 */
data class GamePointsOnDraw(val shared: NonNegativeValue)

/**
 * Represents the points strategies of a game on a forfeit or a timer.
 * @property winner Points that the winner gets.
 * @property forfeiter Points that the forfeiter gets.
 */
data class GamePointsOnForfeitOrTimer(val winner: NonNegativeValue, val forfeiter: NonNegativeValue)
