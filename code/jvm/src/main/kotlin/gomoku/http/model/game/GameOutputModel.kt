package gomoku.http.model.game

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.GameVariant
import gomoku.http.jackson.serializers.BoardSizeSerializer
import gomoku.http.jackson.serializers.InstantSerializer
import gomoku.http.model.JsonOutputModel
import kotlinx.datetime.Instant

class GameOutputModel private constructor(
    val id: Id,
    val state: String,
    val variant: GameVariant,
    @field:JsonSerialize(using = BoardSizeSerializer::class)
    val board: BoardOutputModel,
    @field:JsonSerialize(using = InstantSerializer::class)
    val createdAt: Instant,
    @field:JsonSerialize(using = InstantSerializer::class)
    val updatedAt: Instant,
    val hostId: Int,
    val guestId: Int
) {
    companion object : JsonOutputModel<Game, GameOutputModel> {
        override fun serializeFrom(domainClass: Game): GameOutputModel =
            GameOutputModel(
                id = domainClass.id,
                state = domainClass.state.name,
                variant = domainClass.variant,
                board = BoardOutputModel.serializeFrom(domainClass.board),
                createdAt = domainClass.createdAt,
                updatedAt = domainClass.updatedAt,
                hostId = domainClass.hostId,
                guestId = domainClass.guestId
            )

    }
}