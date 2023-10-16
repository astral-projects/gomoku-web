package gomoku.repository.jdbi.model.game

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.GameState
import gomoku.domain.game.GameVariant
import gomoku.domain.game.OpeningRule
import gomoku.domain.game.Variant
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardSize
import gomoku.repository.jdbi.model.JdbiModel
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.util.*

class JdbiGameWithVariantModel(
    val id: Id,
    val state: GameState,
    @ColumnName("variant_id")
    val variantId: Id,
    val board: Board,
    @ColumnName("created_at")
    val createdAt: Instant,
    @ColumnName("updated_at")
    val updatedAt: Instant,
    @ColumnName("host_id")
    val hostId: Int,
    @ColumnName("guest_id")
    val guestId: Int,
    @ColumnName("name")
    val variantName: String,
    @ColumnName("opening_rule")
    val openingRule: String,
    @ColumnName("board_size")
    val boardSize: String,
) : JdbiModel<Game> {
    override fun toDomainModel(): Game {
        return Game(
            id = id,
            state = state,
            variant = GameVariant(
                id = variantId,
                name = Variant.valueOf(variantName.uppercase(Locale.getDefault())),
                openingRule = OpeningRule.valueOf(openingRule.uppercase(Locale.getDefault())),
                boardSize = BoardSize.valueOf(boardSize.uppercase(Locale.getDefault()))
            ),
            board = board,
            createdAt = createdAt,
            updatedAt = updatedAt,
            hostId = hostId,
            guestId = guestId
        )
    }
}
