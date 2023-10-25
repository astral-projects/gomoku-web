package gomoku.repository.jdbi.model.game

import gomoku.repository.jdbi.model.JdbiModel

class JdbiBooleanModel(
    val value: Int
) : JdbiModel<Boolean> {
    override fun toDomainModel(): Boolean = value > 1
}
