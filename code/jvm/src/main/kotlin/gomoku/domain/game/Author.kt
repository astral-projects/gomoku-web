package gomoku.domain.game

import java.net.URI

/**
 * Represents a game author.
 * @property firstName The first name of the author.
 * @property lastName The last name of the author.
 * @property gitHubUrl The GitHub URL of the author.
 */
class Author(
    val firstName: String,
    val lastName: String,
    val gitHubUrl: URI
)
