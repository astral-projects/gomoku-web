package gomoku.http.model.user

import gomoku.domain.Id
import gomoku.domain.user.Username

class UserHomeOutputModel(
    val id: Id,
    val username: Username
)
