package gomoku.repository.jdbi.model.user

import gomoku.domain.Id
import gomoku.domain.user.Email
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.Username
import gomoku.repository.jdbi.model.JdbiModel
import org.jdbi.v3.core.mapper.reflect.ColumnName

class JdbiUserModel(
    val id: Int,
    val username: String,
    val email: String,
    @ColumnName("password_validation")
    val passwordValidation: String
) : JdbiModel<User> {
    override fun toDomainModel(): User {
        return User(
            id = Id(id),
            username = Username(username),
            email = Email(email),
            passwordValidation = PasswordValidationInfo(passwordValidation)
        )
    }
}
