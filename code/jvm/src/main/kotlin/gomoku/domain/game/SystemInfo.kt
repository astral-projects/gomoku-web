package gomoku.domain.game

import java.net.URI
import java.util.*

object SystemInfo {
    const val GAME_NAME = "Gomoku Royale"
    const val VERSION = "1.0.9"

    val releaseDate = dateFormatted(Date())
    val authors = listOf(
        Author("Diogo", "Rodrigues", URI("https://github.com/Diogofmr")),
        Author("Francisco", "Engenheiro", URI("https://github.com/FranciscoEngenheiro")),
        Author("Tiago", "Frazão", URI("https://github.com/TiagoFrazao01"))
    )

    private fun dateFormatted(date: Date): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(date)
    }
}
