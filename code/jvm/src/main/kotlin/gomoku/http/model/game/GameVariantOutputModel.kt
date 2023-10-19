package gomoku.http.model.game

import gomoku.domain.game.variants.GameVariant
import java.util.*

class GameVariantOutputModel private constructor(
    val id: Int,
    val name: String,
    val openingRule: String,
    val boardSize: Int
) {
    companion object {
        fun serializeFrom(domainClass: GameVariant): GameVariantOutputModel =
            GameVariantOutputModel(
                id = domainClass.id.value,
                name = domainClass.name.name.lowercase(Locale.getDefault()),
                openingRule = domainClass.openingRule.name.lowercase(Locale.getDefault()),
                boardSize = domainClass.boardSize.size
            )
    }
}
