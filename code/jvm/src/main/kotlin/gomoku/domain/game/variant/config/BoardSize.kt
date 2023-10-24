package gomoku.domain.game.variant.config

enum class BoardSize(val size: Int) {
    FIFTEEN(15),
    NINETEEN(19);

    companion object {
        fun fromSize(size: Int): BoardSize = BoardSize.values().find { it.size == size }
            ?: throw IllegalArgumentException("Board size $size is not supported")
    }
}
