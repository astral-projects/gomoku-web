package gomoku.domain.components

/**
 * Marker interface for domain components. Domain components are the building blocks of domain objects
 * that require validation.
 * Typically, they are classes with private constructors and a companion object that
 * provides a factory method for creating instances of the class using the **invoke** operator and
 * the **Either** type to represent the result of the operation.
 *
 * Such classes shouldn't be:
 * - data classes because data classes expose their constructor indirectly through the **copy** method.
 * As such, implementors are advised to override **equals**, **hashCode** and **toString**,
 * to maintain data container behavior.
 * - value classes because value classes, annotated with **@JvmInline**, do not allow,
 * at the time of writing, overriding **equals**, **hashCode** and **toString**.
 *
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
 *    // override equals, hashCode and toString
 *    // other methods
 * }
 */
interface Component
