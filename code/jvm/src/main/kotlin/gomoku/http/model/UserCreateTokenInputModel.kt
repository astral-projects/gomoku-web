package gomoku.http.model

data class UserCreateTokenInputModel(
    val username: String,
    val password: String
)
