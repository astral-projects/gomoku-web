package gomoku.http.model.user

data class UserDB(
    val id: Int,
    val username: String,
    val email: String,
    val passwordValidation: String
)