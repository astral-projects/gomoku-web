package gomoku.domain.game

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import gomoku.domain.game.board.Board
import gomoku.domain.game.board.BoardSize
import gomoku.http.jackson.serializers.BoardSizeSerializer
import gomoku.http.jackson.serializers.GameIdSerializer
import gomoku.http.jackson.serializers.InstantSerializer
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.reflect.ColumnName

data class Game(
    @field:JsonSerialize(using = GameIdSerializer::class)
    val id: GameId,
    val state: GameState,
    @ColumnName("game_variant")
    val variant: GameVariant,
    @ColumnName("opening_rule")
    val openingRule: OpeningRule,
    @field:JsonSerialize(using = BoardSizeSerializer::class)
    @ColumnName("board_size") val boardSize: BoardSize,
    val board: Board,
    @field:JsonSerialize(using = InstantSerializer::class)
    @ColumnName("created_at")
    val createdAt: Instant,
    @field:JsonSerialize(using = InstantSerializer::class)
    @ColumnName("updated_at")
    val updatedAt: Instant,
    @ColumnName("host_id")
    val hostId: Int,
    @ColumnName("guest_id")
    val guestId: Int
)
