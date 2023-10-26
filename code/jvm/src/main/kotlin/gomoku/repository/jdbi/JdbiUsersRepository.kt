package gomoku.repository.jdbi

import gomoku.domain.PaginatedResult
import gomoku.domain.UserAndToken
import gomoku.domain.components.Id
import gomoku.domain.components.NonNegativeValue
import gomoku.domain.components.PositiveValue
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.UserRankInfo
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username
import gomoku.repository.UsersRepository
import gomoku.repository.jdbi.model.JdbiIdModel
import gomoku.repository.jdbi.model.user.JdbiUserAndStatsModel
import gomoku.repository.jdbi.model.user.JdbiUserAndTokenModel
import gomoku.repository.jdbi.model.user.JdbiUserModel
import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory

class JdbiUsersRepository(
    private val handle: Handle
) : UsersRepository {

    override fun getUserByUsername(username: Username): User? =
        handle.createQuery("select * from dbo.Users where username = :username")
            .bind("username", username.value)
            .mapTo<JdbiUserModel>()
            .singleOrNull()?.toDomainModel()

    override fun getUserById(userId: Id): User? =
        handle.createQuery("select * from dbo.Users where id = :id")
            .bind("id", userId.value)
            .mapTo<JdbiUserModel>()
            .singleOrNull()?.toDomainModel()

    override fun storeUser(username: Username, email: Email, passwordValidation: PasswordValidationInfo): Id {
        val userId = handle.createUpdate(
            """
            insert into dbo.Users (username, email, password_validation) values (:username, :email, :password_validation)
            """
        )
            .bind("username", username.value)
            .bind("email", email.value)
            .bind("password_validation", passwordValidation.validationInfo)
            .executeAndReturnGeneratedKeys()
            .mapTo<JdbiIdModel>()
            .one()
            .toDomainModel()

        handle.createUpdate("insert into dbo.Statistics (user_id) values (:user_id)")
            .bind("user_id", userId.value)
            .execute()
        return userId
    }

    override fun isUserStoredByEmail(email: Email): Boolean =
        handle.createQuery("select count(*) from dbo.Users where email = :email")
            .bind("email", email.value)
            .mapTo<Int>()
            .single() == 1

    override fun createToken(token: Token, maxTokens: PositiveValue) {
        val deletions = handle.createUpdate(
            """
            delete from dbo.Tokens 
            where user_id = :user_id and token_validation in (
                    select token_validation from dbo.Tokens 
                    where user_id = :user_id 
                    order by last_used_at desc offset :offset
                )
            """.trimIndent()
        )
            .bind("user_id", token.userId.value)
            .bind("offset", maxTokens.value - 1)
            .execute()

        logger.info("{} tokens deleted when creating new token", deletions)

        handle.createUpdate(
            """
                insert into dbo.Tokens(user_id, token_validation, created_at, last_used_at) 
                values (:user_id, :token_validation, :created_at, :last_used_at)
            """.trimIndent()
        )
            .bind("user_id", token.userId.value)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .execute()
    }

    override fun updateTokenLastUsed(token: Token, now: Instant) {
        handle.createUpdate(
            """
                update dbo.Tokens
                set last_used_at = :last_used_at
                where token_validation = :validation_information
            """.trimIndent()
        )
            .bind("last_used_at", now.epochSeconds)
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): UserAndToken? =
        handle.createQuery(
            """
                select id, username, email, password_validation, token_validation, created_at, last_used_at
                from dbo.Users as users 
                inner join dbo.Tokens as tokens 
                on users.id = tokens.user_id
                where token_validation = :validation_information
            """.trimIndent()
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<JdbiUserAndTokenModel>()
            .singleOrNull()?.toDomainModel()

    override fun revokeToken(tokenValidationInfo: TokenValidationInfo): Boolean =
        handle.createUpdate(
            """
                delete from dbo.Tokens
                where token_validation = :validation_information
            """.trimIndent()
        )
            .bind("validation_information", tokenValidationInfo.validationInfo)
            .execute()
            .let { it == 1 }

    override fun getUsersStats(offset: NonNegativeValue, limit: PositiveValue): PaginatedResult<UserRankInfo> {
        val totalItems = handle.createQuery("select count(*) from dbo.Statistics")
            .mapTo<Int>()
            .single()
        val result = handle.createQuery(
            """
                select id, username, email, points, rank() over(order by points desc) as rank, games_played, games_won, games_drawn
                from dbo.Users as users 
                inner join dbo.Statistics as stats 
                on users.id = stats.user_id
                offset :offset 
                limit :limit
            """.trimIndent()
        )
            .bind("offset", offset.value)
            .bind("limit", limit.value)
            .mapTo<JdbiUserAndStatsModel>()
            .map { it.toDomainModel() }
        return PaginatedResult.create(result.toList(), totalItems, offset.value, limit.value)
    }

    override fun getUserStats(userId: Id): UserRankInfo? =
        handle.createQuery(
            """
                select id, username, email, points, rank() over(order by points desc) as rank, games_played, games_won, games_drawn
                from dbo.Users as users 
                inner join dbo.Statistics as stats 
                on users.id = stats.user_id
                where users.id = :user_id
            """.trimIndent()
        )
            .bind("user_id", userId.value)
            .mapTo<JdbiUserAndStatsModel>()
            .singleOrNull()?.toDomainModel()

    override fun editUser(userId: Id): User? {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }
}
