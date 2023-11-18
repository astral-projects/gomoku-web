package gomoku.http.model

data class TestGameModel(
    val id: Int,
    val state: State,
    val variant: Variant,
    val board: Board,
    val createdAt: String,
    val updatedAt: String,
    val hostId: Int,
    val guestId: Int,
) {
    data class State(
        val name: String,
    )

    data class Variant(
        val id: Int,
        val name: String,
        val openingRule: String,
        val boardSize: Int,
    )

    data class Board(
        val grid: List<String>,
        val turn: Turn,
    ) {
        data class Turn(
            val player: String,
            val timeLeftInSec: TimeLeftInSec,
        ) {
            data class TimeLeftInSec(
                val value: Int,
            )
        }
    }
}

