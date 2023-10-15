package gomoku.domain.game

import java.util.*

enum class GameState {
    IN_PROGRESS,
    FINISHED;

    override fun toString(): String = GameState.valueOf(this.name).name.lowercase(Locale.getDefault())
}