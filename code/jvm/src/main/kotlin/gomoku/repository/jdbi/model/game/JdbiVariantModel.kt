package gomoku.repository.jdbi.model.game

import gomoku.domain.Id
import gomoku.domain.game.board.BoardSize
import gomoku.domain.game.variants.AcceptableVariant
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.game.variants.OpeningRule
import gomoku.repository.jdbi.model.JdbiModel
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiVariantModel(
    val id: Int,
    val name: AcceptableVariant,
    @ColumnName("opening_rule")
    val openingRule: String,
    @ColumnName("board_size")
    val boardSize: Int
) : JdbiModel<GameVariant> {
    override fun toDomainModel(): GameVariant {
        return GameVariant(
            id = Id(id),
            name = name,
            openingRule = OpeningRule.valueOf(openingRule),
            boardSize = BoardSize.fromSize(boardSize)
        )
    }
}
