package gomoku

// Constants
private const val KEY_DB_URL = "DB_URL"

/**
 * Provides access to project environment variables.
 */
object Environment {

    /**
     * Returns the value of the environment variable [KEY_DB_URL], or throws an exception if it is not set.
     */
    fun getDbUrl() = System.getenv(KEY_DB_URL)
        ?: throw Exception("Missing env var $KEY_DB_URL")
}
