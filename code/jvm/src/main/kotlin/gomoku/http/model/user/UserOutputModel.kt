package gomoku.http.model.user

import gomoku.domain.user.User
import gomoku.http.model.JsonOutputModel

class UserOutputModel(
    val username: String,
    val email: String
) {
    companion object : JsonOutputModel<User, UserOutputModel> {
        override fun serializeFrom(domainClass: User): UserOutputModel {
            return UserOutputModel(
                username = domainClass.username.value,
                email = domainClass.email.value
            )
        }
    }
}
