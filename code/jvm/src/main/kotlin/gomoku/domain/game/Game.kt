package gomoku.domain.game

data class Game(
    val game_id: Int,
    val state: String,
    val game_variant: String,
    val opening_rule: String,
    val board_size: Int,
    val board: String,
    val created: Int,
    val updated: Int,
    val host: Int,
    val guest: Int,
)
