package gomoku.repository.jdbi.model.game

import gomoku.domain.Id
import gomoku.domain.game.variants.GameVariant
import gomoku.domain.game.variants.OpeningRule
import gomoku.domain.game.variants.AcceptableVariant
import gomoku.domain.game.board.BoardSize
import gomoku.repository.jdbi.model.JdbiModel
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiVariantModel (
    val id: Id,
    val name: AcceptableVariant,
    @ColumnName("opening_rule")
    val openingRule: OpeningRule,
    @ColumnName("board_size")
    val boardSize: BoardSize
) : JdbiModel<GameVariant> {
    override fun toDomainModel(): GameVariant {
        return GameVariant(
            id = id,
            name = name,
            openingRule = openingRule,
            boardSize = boardSize
        )
    }
}