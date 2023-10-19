package gomoku.http.model.user

import gomoku.domain.user.UserRankInfo
import gomoku.http.model.JsonOutputModel

class UserRankOutputModel private constructor(
    val username: String,
    val email: String,
    val points: Int,
    val rank: Int,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int
) {
    companion object : JsonOutputModel<UserRankInfo, UserRankOutputModel> {
        override fun serializeFrom(domainClass: UserRankInfo): UserRankOutputModel {
            return UserRankOutputModel(
                username = domainClass.username.value,
                email = domainClass.email.value,
                points = domainClass.points,
                rank = domainClass.rank,
                gamesPlayed = domainClass.gamesPlayed,
                wins = domainClass.wins,
                losses = domainClass.losses
            )
        }
    }
}
