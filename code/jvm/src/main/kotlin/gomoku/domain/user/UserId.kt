package gomoku.domain.user

class UserId (val value: Int) {
    init {
        require(value > 0) { "User id must be positive" }
    }
}
