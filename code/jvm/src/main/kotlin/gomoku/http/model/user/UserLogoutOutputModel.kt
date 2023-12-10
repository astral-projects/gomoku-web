package gomoku.http.model.user

data class UserLogoutOutputModel(
    val message: String = "User logged out successfully, token was revoked."
)
