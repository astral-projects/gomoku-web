package gomoku.utils

/**
 * Marks test classes and functions that represent intrusive tests. Intrusive tests are tests
 * that alter the database state and therefore should be run with caution and in a database that
 * is not in production.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
internal annotation class IntrusiveTests
