package gomoku.http.model.user

import gomoku.domain.user.UserId
import gomoku.domain.user.Username

class UserHomeOutputModel(
    val id: UserId,
    val username: Username
)
