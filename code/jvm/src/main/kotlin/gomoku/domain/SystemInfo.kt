package gomoku.domain

import gomoku.domain.game.Author
import java.net.URI
import java.util.*

/**
 * Provides information about the system.
 */
object SystemInfo {
    const val GAME_NAME = "Gomoku Royale API"
    const val VERSION = "0.0.1"
    const val DESCRIPTION =
        "Gomoku Royale is an online multiplayer strategy game where players compete to connect five of their pieces in a row, column or diagonally."

    val releaseDate = dateFormatted(Date())
    val authors = listOf(
        Author("Diogo", "Rodrigues", URI("https://github.com/Diogofmr")),
        Author("Tiago", "Frazão", URI("https://github.com/TiagoFrazao01")),
        Author("Francisco", "Engenheiro", URI("https://github.com/FranciscoEngenheiro"))
    )

    private fun dateFormatted(date: Date): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(date)
    }
}
