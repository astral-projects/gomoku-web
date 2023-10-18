package gomoku.repository.jdbi.model

import gomoku.domain.Id

class JdbiIdModel(
    val id: Int
) : JdbiModel<Id> {
    override fun toDomainModel(): Id = Id(value = id)
}