package gomoku.repository.jdbi.model

/**
 * A functional interface for mapping a database model into a domain model. Implementations of this interface
 * should define the necessary logic to convert a database-specific model into the corresponding domain model.
 */
@FunctionalInterface
fun interface JdbiModel<R> {
    fun toDomainModel(): R
}
