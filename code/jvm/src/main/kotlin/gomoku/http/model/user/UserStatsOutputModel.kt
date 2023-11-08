package gomoku.http.model.user

import gomoku.domain.user.UserStatsInfo
import gomoku.http.model.JsonOutputModel

class UserStatsOutputModel private constructor(
    val id: Int,
    val username: String,
    val email: String,
    val points: Int,
    val rank: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int
) {
    companion object : JsonOutputModel<UserStatsInfo, UserStatsOutputModel> {
        override fun serializeFrom(domainClass: UserStatsInfo): UserStatsOutputModel {
            return UserStatsOutputModel(
                id = domainClass.id.value,
                username = domainClass.username.value,
                email = domainClass.email.value,
                points = domainClass.points.value,
                rank = domainClass.rank.value,
                gamesPlayed = domainClass.gamesPlayed.value,
                wins = domainClass.wins.value,
                draws = domainClass.draws.value,
                losses = domainClass.losses.value
            )
        }
    }
}
