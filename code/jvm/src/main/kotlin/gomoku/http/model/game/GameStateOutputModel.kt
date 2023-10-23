package gomoku.http.model.game

import java.util.*

class GameStateOutputModel private constructor(
    val name: String
) {
    companion object {
        fun serializeFrom(domainClass: gomoku.domain.game.GameState): GameStateOutputModel =
            GameStateOutputModel(
                name = domainClass.name.lowercase(Locale.getDefault())
            )
    }
}
