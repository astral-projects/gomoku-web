package gomoku.http.model.user

import gomoku.domain.user.User

data class UserOutputModel(
    val id: Int,
    val username: String,
    val email: String
) {
    companion object {
        fun serializeFrom(domainClass: User): UserOutputModel {
            return UserOutputModel(
                id = domainClass.id.value,
                username = domainClass.username.value,
                email = domainClass.email.value
            )
        }
    }
}
