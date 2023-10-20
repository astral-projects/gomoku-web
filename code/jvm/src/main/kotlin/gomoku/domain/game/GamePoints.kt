package gomoku.domain.game

import gomoku.domain.NonNegativeValue
import gomoku.domain.PositiveValue

class GamePoints {
    val winner_points :PositiveValue
        get() =  PositiveValue(300)
    val loser_points
        get() =  PositiveValue(100)
    val draw_points
        get() =  PositiveValue(50)
    val itsWin
        get() =  NonNegativeValue(1)
    val itsDraw
        get()  = NonNegativeValue(0)


}
