package gomoku.http.model.user

data class GetUserOutputModel(
    val id: Int,
    val username: String,
    val email: String
)
