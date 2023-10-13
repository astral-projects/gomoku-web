package gomoku.domain

interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
