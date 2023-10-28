package gomoku.repository.jdbi.model.user

import gomoku.domain.UserAndToken
import gomoku.domain.components.Id
import gomoku.domain.token.Token
import gomoku.domain.token.TokenValidationInfo
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username
import gomoku.repository.jdbi.model.JdbiModel
import gomoku.utils.get
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiUserAndTokenModel(
    val id: Int,
    val username: String,
    val email: String,
    @ColumnName("password_validation")
    val passwordValidation: String,
    @ColumnName("token_validation")
    val tokenValidation: String,
    @ColumnName("created_at")
    val createdAt: Long,
    @ColumnName("last_used_at")
    val lastUsedAt: Long
) : JdbiModel<UserAndToken> {
    override fun toDomainModel(): UserAndToken {
        return User(
            id = Id(id).get(),
            username = Username(username).get(),
            email = Email(email).get(),
            passwordValidation = PasswordValidationInfo(passwordValidation)
        ) to Token(
            tokenValidationInfo = TokenValidationInfo(tokenValidation),
            userId = Id(id).get(),
            createdAt = Instant.fromEpochSeconds(createdAt),
            lastUsedAt = Instant.fromEpochSeconds(lastUsedAt)
        )
    }
}
