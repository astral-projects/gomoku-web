package gomoku.repository.jdbi.model.user

import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.user.UserStatsInfo
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username
import gomoku.repository.jdbi.model.JdbiModel
import gomoku.utils.get
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiUserAndStatsModel(
    val id: Int,
    val username: String,
    val email: String,
    val points: Int,
    val rank: Int,
    @ColumnName("games_played")
    val gamesPlayed: Int,
    @ColumnName("games_won")
    val wins: Int,
    @ColumnName("games_drawn")
    val draws: Int
) : JdbiModel<UserStatsInfo> {
    override fun toDomainModel(): UserStatsInfo =
        UserStatsInfo(
            id = Id(id).get(),
            username = Username(username).get(),
            email = Email(email).get(),
            points = NonNegativeValue(points).get(),
            rank = NonNegativeValue(rank).get(),
            gamesPlayed = NonNegativeValue(gamesPlayed).get(),
            wins = NonNegativeValue(wins).get(),
            draws = NonNegativeValue(draws).get(),
            losses = NonNegativeValue(gamesPlayed - wins - draws).get()
        )
}
