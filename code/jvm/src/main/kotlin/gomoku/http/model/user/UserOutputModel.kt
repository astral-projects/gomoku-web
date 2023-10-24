package gomoku.http.model.user

import gomoku.domain.user.User
import gomoku.http.model.JsonOutputModel

class UserOutputModel(
    val id: Int,
    val username: String,
    val email: String
) {
    companion object : JsonOutputModel<User, UserOutputModel> {
        override fun serializeFrom(domainClass: User): UserOutputModel {
            return UserOutputModel(
                id = domainClass.id.value,
                username = domainClass.username.value,
                email = domainClass.email.value
            )
        }
    }
}
