package gomoku.utils

/**
 * An annotation to mark code that has not been tested and should not be used in production.
 *
 * This annotation serves the following purposes:
 * - It helps developers identify code that requires testing.
 * - It indicates that the code is not production-ready and should be considered unreliable.
 * - It encourages the removal of the annotation once the code has undergone sufficient testing and is deemed stable.
 */
// @MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class NotTested
