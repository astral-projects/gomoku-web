package gomoku.domain.user

class Email(
    val value: String
) {

    init {
        // Email format regex from https://www.regular-expressions.info/email.html
        val emailFormat = Regex(("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$"))
        require(value.matches(emailFormat)) { "Invalid email format" }
    }
}
