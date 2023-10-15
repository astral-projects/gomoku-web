package gomoku.http.model.user

data class UserCreateInputModel(
    val username: String,
    val email: String,
    val password: String
)
