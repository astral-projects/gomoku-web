package gomoku.domain.user

data class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username must not be blank" }
    }
}
