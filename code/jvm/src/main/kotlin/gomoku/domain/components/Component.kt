package gomoku.domain.components

/**
 * Marker interface for domain components. Domain components are the building blocks of domain objects
 * that require validation.
 * Typically, they are classes with private constructors and a companion object that
 * provides a factory method for creating instances of the class using the **invoke** operator and
 * the **Either** type to represent the result of the operation.
 * Example:
 * ```
 * class Username private constructor(val value: String) : Component {
 *    companion object {
 *      operator fun invoke(value: String): Either<UsernameError, Username> =
 *          when {
 *              value.isBlank() -> Failure(UsernameError.Blank)
 *              value.length < 3 -> Failure(UsernameError.TooShort)
 *              else -> Success(Username(value))
 *          }
 *       }
 *    }
 *    // other methods
 * }
 */
interface Component
