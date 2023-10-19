package gomoku.domain.user

data class UserRankInfo(
    val username: Username,
    val email: Email,
    val points: Int,
    val rank: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int
)
