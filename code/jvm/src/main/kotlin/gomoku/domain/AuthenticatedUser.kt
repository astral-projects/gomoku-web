package gomoku.domain

class AuthenticatedUser(
    val user: User,
    val token: String
)
