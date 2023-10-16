package gomoku.repository.jdbi.model

import gomoku.domain.SerializableDomainModel

@FunctionalInterface
interface JdbiModel<R : SerializableDomainModel> {
    fun toDomainModel(): R
}