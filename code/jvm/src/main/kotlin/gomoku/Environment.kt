package gomoku

// Constants
private const val KEY_DB_URL = "DB_URL"

/**
 * Returns the value of the environment variable [KEY_DB_URL], or throws an exception.
 */
object Environment {

    fun getDbUrl() = System.getenv(KEY_DB_URL) ?: throw Exception("Missing env var $KEY_DB_URL")
}
