package gomoku.domain.user

import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username

/**
 * Represents the information of a user that can be used to display its stats.
 * @property id The Id of the user.
 * @property username The username of the user.
 * @property email The email of the user.
 * @property points The points of the user.
 * @property rank The rank of the user.
 * @property gamesPlayed The number of games played by the user.
 * @property wins The number of games won by the user.
 * @property draws The number of games drawn by the user.
 * @property losses The number of games lost by the user.
 */
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
