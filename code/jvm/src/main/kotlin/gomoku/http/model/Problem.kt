package gomoku.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

data class Problem (
    val type: URI,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: URI? = null
) {
    fun toResponse() = ResponseEntity
        .status(status)
        .header("Content-Type", MEDIA_TYPE)
        .body<Any>(this)

    companion object {

        const val baseUrl = "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/"
        const val MEDIA_TYPE = "application/problem+json"

        val insecurePassword = URI("${baseUrl}insecure-password")
        val usernameIsInvalid = URI("${baseUrl}username-is-invalid")
        val passwordIsInvalid = URI("${baseUrl}password-is-invalid")
        val invalidRequestContent = URI("${baseUrl}invalid-request-content")
        val gameNotFound = URI("${baseUrl}game-not-found")
        val userAlreadyInLobby = URI("${baseUrl}user-already-in-lobby")
        val userIsNotTheHost = URI("${baseUrl}user-is-not-the-host")
        val invalidMove = URI("${baseUrl}invalid-move")
        val userAlreadyInGame = URI("${baseUrl}user-already-in-game")
        val usernameAlreadyExists = URI("${baseUrl}username-already-exists")
        val emailAlreadyExists = URI("${baseUrl}email-already-exists")
        val userNotFound = URI("${baseUrl}user-not-found")
        val gameVariantNotFound = URI("${baseUrl}game-variant-not-found")
        val tokenIsInvalid = URI("${baseUrl}token-is-invalid")
    }
}
