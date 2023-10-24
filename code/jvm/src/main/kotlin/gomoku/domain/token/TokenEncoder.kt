package gomoku.domain.token

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
