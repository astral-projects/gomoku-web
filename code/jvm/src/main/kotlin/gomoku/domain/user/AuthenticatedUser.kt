package gomoku.domain.user

data class AuthenticatedUser(
    val user: User,
    val token: String
)
