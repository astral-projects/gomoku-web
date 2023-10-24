package gomoku.repository.jdbi.model

import gomoku.domain.Id
import gomoku.utils.get

class JdbiIdModel(
    val id: Int
) : JdbiModel<Id> {
    override fun toDomainModel(): Id = Id(value = id).get()
}
