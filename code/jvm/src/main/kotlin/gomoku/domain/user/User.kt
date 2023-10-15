package gomoku.domain.user

private const val MIN_USERNAME_LENGTH = 5
private const val MAX_USERNAME_LENGTH = 20

data class User(
    val id: UserId,
    val username: Username,
    val email: Email,
    val passwordValidation: PasswordValidationInfo
) {
    init {
        require(username.value.isNotBlank())
        require(username.value.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH)
    }
}
