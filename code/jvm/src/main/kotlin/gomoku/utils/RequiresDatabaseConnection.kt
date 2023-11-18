package gomoku.utils

/**
 * Marks classes and functions that require a database connection prior to running code.
 * In the current implementation, a few things are necessary for the database to be available:
 * - [Docker Compose](https://www.docker.com/products/docker-compose) is
 * used to run a PostgreSQL database in a Docker container, and as such, it must be
 * installed and running on the system.
 * - The **Environment** object must be able to retrieve the database URL from an environment
 * variable named **DB_URL**.
 * - If another postgres database is running on the system, it must be stopped before running
 * the tests, as the port used by the database is probably already in use.
 * On windows, the following actions can be used to stop the database:
 * ```
 * windows + r -> services.msc -> PostgreSQL Server <version> -> stop
 * ```
 * The database is launched and waiting with the following command:
 * ```
 * ./gradlew dbTestsWait
 * ```
 *
 * The database can be stopped with the following command:
 * ```
 * ./gradlew dbTestsStop
 * ```
 * More commands are available in **build.gradle.kts** file of this module.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class RequiresDatabaseConnection
