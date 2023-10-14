package gomoku.domain.user

private const val MIN_USERNAME_LENGTH = 5
private const val MAX_USERNAME_LENGTH = 20

data class User(
    val id: Int,
    val username: String,
    val passwordValidation: PasswordValidationInfo
) {
    init {
        require(username.isNotBlank())
        require(username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH)
    }
}
