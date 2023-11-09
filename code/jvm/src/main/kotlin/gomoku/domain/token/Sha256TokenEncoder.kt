package gomoku.domain.token

import java.security.MessageDigest
import java.util.*

// Constants
private const val ALGORITHM = "SHA256"

/**
 * A token encoder that [MessageDigest] to hash the token.
 */
class Sha256TokenEncoder : TokenEncoder {

    override fun createValidationInformation(token: String) = TokenValidationInfo(hash(token))

    /**
     * Hashes the given input using SHA256 and returns the result as a Base64 encoded string.
     */
    private fun hash(input: String): String {
        val messageDigest = MessageDigest.getInstance(ALGORITHM)
        return Base64.getUrlEncoder().encodeToString(
            messageDigest.digest(Charsets.UTF_8.encode(input).array())
        )
    }
}
