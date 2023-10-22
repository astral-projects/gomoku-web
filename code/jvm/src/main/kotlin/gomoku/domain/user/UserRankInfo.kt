package gomoku.domain.user

import gomoku.domain.Id
import gomoku.domain.NonNegativeValue

data class UserRankInfo(
    val id: Id,
    val username: Username,
    val email: Email,
    val points: NonNegativeValue,
    val rank: NonNegativeValue,
    val gamesPlayed: NonNegativeValue,
    val wins: NonNegativeValue,
    val draws: NonNegativeValue,
    val losses: NonNegativeValue
)
