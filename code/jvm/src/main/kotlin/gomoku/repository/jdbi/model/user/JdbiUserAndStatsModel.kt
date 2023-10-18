package gomoku.repository.jdbi.model.user

import gomoku.domain.user.Email
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.Username
import gomoku.repository.jdbi.model.JdbiModel
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiUserAndStatsModel(
    val username: String,
    val email: String,
    val points: Int,
    val rank: Int,
    // TODO("create a domain class that receives positive or zero values only, like in Email and related components")
    @ColumnName("games_played")
    val gamesPlayed: Int,
    @ColumnName("games_won")
    val wins: Int,
    val losses: Int
) : JdbiModel<UserRankInfo> {
    override fun toDomainModel(): UserRankInfo =
        UserRankInfo(
            username = Username(username),
            email = Email(email),
            points = points,
            rank = rank,
            gamesPlayed = gamesPlayed,
            wins = wins,
            losses = gamesPlayed - wins
        )
}
