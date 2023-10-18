package gomoku.http.model

/**
 * A functional interface that transforms a domain class into an output model, which is then
 * serialized to JSON using the Jackson library. Implementations of this interface are typically provided
 * by the companion object of the output model class, which employs the private constructor of the output model class.
 */
@FunctionalInterface
fun interface JsonOutputModel<R, S> {
    fun serializeFrom(domainClass: R): S
}
