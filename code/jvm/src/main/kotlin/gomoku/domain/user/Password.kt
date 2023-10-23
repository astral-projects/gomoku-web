package gomoku.domain.user

private const val MAX_PASSWORD_LENGTH = 40
private const val MIN_PASSWORD_LENGTH = 8

data class Password(
    val value: String
) {

    companion object {
        fun isSafe(value: String) = value.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
    }

    init {
        require(value.isNotBlank() && value.isNotEmpty())
        require(isSafe(value))
    }
}
