package gomoku.domain.game

import gomoku.domain.NonNegativeValue

data class GamePoints(val onFinish: GamePointsOnWin, val onDraw: GamePointsOnDraw, val onForfeitOrTimer: GamePointsOnForfeitOrTimer)
data class GamePointsOnWin(val winner: NonNegativeValue, val loser: NonNegativeValue)
data class GamePointsOnDraw(val shared: NonNegativeValue)
data class GamePointsOnForfeitOrTimer(val winner: NonNegativeValue, val forfeiter: NonNegativeValue)
