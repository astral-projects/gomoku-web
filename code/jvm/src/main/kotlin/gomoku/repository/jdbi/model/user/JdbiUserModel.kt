package gomoku.repository.jdbi.model.user

import gomoku.domain.components.Id
import gomoku.domain.user.PasswordValidationInfo
import gomoku.domain.user.User
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username
import gomoku.repository.jdbi.model.JdbiModel
import gomoku.utils.get
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
            id = Id(id).get(),
            username = Username(username).get(),
            email = Email(email).get(),
            passwordValidation = PasswordValidationInfo(passwordValidation)
        )
    }
}
