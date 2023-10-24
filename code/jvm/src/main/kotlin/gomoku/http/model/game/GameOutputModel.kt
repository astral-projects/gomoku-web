package gomoku.http.model.game

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.Game
import gomoku.http.jackson.serializers.InstantSerializer
import gomoku.http.model.JsonOutputModel
import kotlinx.datetime.Instant

class GameOutputModel private constructor(
    val id: Int,
    val state: GameStateOutputModel,
    val variant: GameVariantOutputModel,
    val board: BoardOutputModel,
    @field:JsonSerialize(using = InstantSerializer::class)
    val createdAt: Instant,
    @field:JsonSerialize(using = InstantSerializer::class)
    val updatedAt: Instant,
    val hostId: Int,
    val guestId: Int
) {
    companion object : JsonOutputModel<Game, GameOutputModel> {
        override fun serializeFrom(domainClass: Game): GameOutputModel {
            return GameOutputModel(
                id = domainClass.id.value,
                state = GameStateOutputModel.serializeFrom(domainClass.state),
                variant = GameVariantOutputModel.serializeFrom(domainClass.variant),
                board = BoardOutputModel.serializeFrom(domainClass.board),
                createdAt = domainClass.createdAt,
                updatedAt = domainClass.updatedAt,
                hostId = domainClass.hostId.value,
                guestId = domainClass.guestId.value
            )
        }
    }
}
