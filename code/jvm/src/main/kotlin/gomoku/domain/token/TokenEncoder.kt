package gomoku.domain.token

/**
 * Functional interface for encoding tokens.
 */
@FunctionalInterface
fun interface TokenEncoder {
    /**
     * Creates the validation information for the given token.
     */
    fun createValidationInformation(token: String): TokenValidationInfo
}
