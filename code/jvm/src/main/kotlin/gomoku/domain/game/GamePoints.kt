package gomoku.domain.game

class GamePoints {
    companion object {
        const val WINNER_POINTS = 300
        const val LOSER_POINTS = 100
        const val DRAW_POINTS = 50
    }

    fun getWinnerPoints(): Int {
        return WINNER_POINTS
    }

    fun getLoserPoints(): Int {
        return LOSER_POINTS
    }

    fun getDrawPoints(): Int {
        return DRAW_POINTS
    }
}
