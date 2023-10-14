package gomoku.domain.user

import gomoku.domain.user.User

class AuthenticatedUser(
    val user: User,
    val token: String
)
