package gomoku.domain.game.board

enum class Player {
    w,
    b
}
fun findPlayer(s: String): Player? {
    val parts = s.split("-")
    return if (parts.size > 1) {
        when (parts[1].toUpperCase()) {
            "W" -> Player.w
            "B" -> Player.b
            else -> null
        }
    } else {
        null
    }
}
