package gomoku.domain.game.variants

import org.springframework.stereotype.Component

@Component
class Variant(
    val variants: List<GameVariant>
) {
    fun getVariant(variant: GameVariant): GameVariant {
        return variants.first { it.name == variant.name }
    }
}