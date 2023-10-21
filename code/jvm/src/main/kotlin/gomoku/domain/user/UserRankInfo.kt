package gomoku.domain.user

import gomoku.domain.NonNegativeValue

data class UserRankInfo(
    val username: Username,
    val email: Email,
    val points: NonNegativeValue,
    val rank: NonNegativeValue,
    val gamesPlayed: NonNegativeValue,
    val wins: NonNegativeValue,
    val losses: NonNegativeValue
)
