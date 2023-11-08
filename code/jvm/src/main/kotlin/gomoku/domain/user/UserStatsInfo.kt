package gomoku.domain.user

import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username

data class UserStatsInfo(
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
