package gomoku.domain.user

data class UserRankInfo(
    val username: Username,
    val email: Email,
    val rankPosition: Int,
    val wins: Int,
    val losses: Int,
    val points: Int,
    val gamesPlayed: Int
)