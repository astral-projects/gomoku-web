package gomoku.repository.jdbi.model.user

import gomoku.domain.NonNegativeValue
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
    @ColumnName("games_played")
    val gamesPlayed: Int,
    @ColumnName("games_won")
    val wins: Int
) : JdbiModel<UserRankInfo> {
    override fun toDomainModel(): UserRankInfo =
        UserRankInfo(
            username = Username(username),
            email = Email(email),
            points = NonNegativeValue(points),
            rank = NonNegativeValue(rank),
            gamesPlayed = NonNegativeValue(gamesPlayed),
            wins = NonNegativeValue(wins),
            losses = NonNegativeValue(gamesPlayed - wins)
        )
}
