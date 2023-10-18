package gomoku.domain.user

data class Email(
    val value: String
) {

    companion object {
        const val emailFormat = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"
    }

    init {
        require(value.matches(Regex(emailFormat))) { "Invalid email format" }
    }
}
