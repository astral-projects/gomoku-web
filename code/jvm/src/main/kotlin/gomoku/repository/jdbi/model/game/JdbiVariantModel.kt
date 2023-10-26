package gomoku.repository.jdbi.model.game

import gomoku.domain.components.Id
import gomoku.domain.game.variant.GameVariant
import gomoku.domain.game.variant.config.BoardSize
import gomoku.domain.game.variant.config.OpeningRule
import gomoku.domain.game.variant.config.VariantName
import gomoku.repository.jdbi.model.JdbiModel
import gomoku.utils.get
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiVariantModel(
    val id: Int,
    val name: String,
    @ColumnName("opening_rule")
    val openingRule: String,
    @ColumnName("board_size")
    val boardSize: Int
) : JdbiModel<GameVariant> {
    override fun toDomainModel(): GameVariant {
        return GameVariant(
            id = Id(id).get(),
            name = VariantName.valueOf(name),
            openingRule = OpeningRule.valueOf(openingRule),
            boardSize = BoardSize.fromSize(boardSize)
        )
    }
}
