package gomoku.repository.jdbi

import gomoku.domain.PaginatedResult
import gomoku.domain.UserAndToken
import gomoku.domain.components.Id
import gomoku.domain.components.PositiveValue
import gomoku.domain.components.Term
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.UserStatsInfo
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

    override fun getUsersStats(page: PositiveValue, itemsPerPage: PositiveValue): PaginatedResult<UserStatsInfo> {
        val result: List<UserStatsInfo> = handle.createQuery(
            """
        SELECT id, username, email, points, row_number() OVER (ORDER BY points DESC) AS rank, games_played, games_won, games_drawn
        FROM dbo.Users AS users
        INNER JOIN dbo.Statistics AS stats ON users.id = stats.user_id
        ORDER BY points DESC
        OFFSET :offset ROWS
        FETCH NEXT :limit ROWS ONLY
            """.trimIndent()
        )
            .bind("limit", itemsPerPage.value)
            .bind("offset", ((page.value - 1) * itemsPerPage.value))
            .mapTo<JdbiUserAndStatsModel>()
            .map { it.toDomainModel() }.toList()

        val totalItems = handle.createQuery(
            """
        SELECT COUNT(*) AS total_items
        FROM dbo.Users AS users
        INNER JOIN dbo.Statistics AS stats ON users.id = stats.user_id
            """.trimIndent()
        )
            .mapTo<Int>()
            .single().toInt()

        return PaginatedResult.create(result, totalItems, page.value, itemsPerPage.value)
    }

    override fun getUserStats(userId: Id): UserStatsInfo? =
        handle.createQuery(
            """
            WITH RankedUsers AS (
                SELECT users.id, users.username, users.email, stats.points, 
                       row_number() OVER (ORDER BY stats.points DESC) as rank, 
                       stats.games_played, stats.games_won, stats.games_drawn
                FROM dbo.Users AS users 
                INNER JOIN dbo.Statistics AS stats 
                ON users.id = stats.user_id
            )
            SELECT *
            FROM RankedUsers
            WHERE id = :user_id

            """.trimIndent()
        )
            .bind("user_id", userId.value)
            .mapTo<JdbiUserAndStatsModel>()
            .singleOrNull()?.toDomainModel()

    override fun getUserStatsByTerm(
        term: Term,
        page: PositiveValue,
        itemsPerPage: PositiveValue
    ): PaginatedResult<UserStatsInfo> {
        val termSQLFormat = "%${term.value}%"
        val result = handle.createQuery(
            """
            WITH RankedUsers AS (
                SELECT stats.points, stats.games_drawn, stats.games_played, stats.games_won, 
                   ROW_NUMBER() OVER (ORDER BY stats.points DESC) as rank, 
                   users.id, users.username, users.email
                FROM dbo.Statistics AS stats
                INNER JOIN dbo.Users AS users ON stats.user_id = users.id
                ORDER BY stats.points DESC
            )
            SELECT *
            FROM RankedUsers
            WHERE username LIKE :term
            OFFSET :offset
            LIMIT :limit;
            """.trimIndent()
        )
            .bind("term", termSQLFormat)
            .bind("limit", itemsPerPage.value)
            .bind("offset", ((page.value - 1) * itemsPerPage.value))
            .mapTo<JdbiUserAndStatsModel>()
            .map { it.toDomainModel() }.toList()

        val totalItems = handle.createQuery(
            """
            SELECT COUNT(*) AS total_items
            FROM dbo.Statistics AS stats
            INNER JOIN dbo.Users AS users ON stats.user_id = users.id
            WHERE users.username LIKE :term
            """.trimIndent()
        )
            .bind("term", termSQLFormat)
            .mapTo<Int>()
            .single().toInt()

        return PaginatedResult.create(result, totalItems, page.value, itemsPerPage.value)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JdbiUsersRepository::class.java)
    }
}
