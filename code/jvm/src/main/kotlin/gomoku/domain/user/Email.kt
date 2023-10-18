package gomoku.domain.user

data class Email (
    val value: String
) {

    companion object {
        val emailFormat = Regex(("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"))
    }

    init {
        require(value.matches(emailFormat)) { "Invalid email format" }
    }
}
