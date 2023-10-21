package gomoku.domain.user

private const val MAX_USERNAME_LENGTH = 30
private const val MIN_USERNAME_LENGTH = 5

data class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username must not be blank" }
        require(value.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH) {
            "Username must be between $MIN_USERNAME_LENGTH and $MAX_USERNAME_LENGTH characters long"
        }
    }
}
