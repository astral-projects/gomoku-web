package gomoku.domain.game

data class GameId(val id: Int) {
    init {
        require(id > 0) { "Game id must be positive" }
    }
}
