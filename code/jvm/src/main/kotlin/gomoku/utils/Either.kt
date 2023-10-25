package gomoku.utils

/**
 * Sum type to represent the success or failure in a given operation.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/**
 * Used to represent a sucess path in a given operation, which is
 * associated with [Either.Right] class
 */
fun <R> success(value: R) = Either.Right(value)

/**
 * Used to represent a failure path in a given operation, which is
 * associated with [Either.Left] class.
 */
fun <L> failure(error: L) = Either.Left(error)

/**
 * Returns the value of [Either.Right] class, or throws an exception
 */
fun <L, R> Either<L, R>.get(): R = when (this) {
    is Failure -> throw IllegalArgumentException("Either.Left has no value")
    is Success -> this.value
}

typealias Success<S> = Either.Right<S>
typealias Failure<F> = Either.Left<F>
