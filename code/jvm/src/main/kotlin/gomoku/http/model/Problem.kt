package gomoku.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val baseUrl = "https://github.com/2023-daw-leic51d-14/code/jvm/docs/problems/"

        // TODO("make a problem json class")
        const val MEDIA_TYPE = "application/problem+json"
        fun response(status: Int, problemType: Problem) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problemType)
        val userAlreadyExists = Problem(URI("${baseUrl}user-already-exists"))
        val insecurePassword = Problem(URI("${baseUrl}insecure-password"))
        val usernameIsInvalid = Problem(URI("${baseUrl}username-is-invalid"))
        val passwordIsInvalid = Problem(URI("${baseUrl}password-is-invalid"))
        val invalidRequestContent = Problem(URI("${baseUrl}invalid-request-content"))
        val gameNotFound = Problem(URI("${baseUrl}game-not-found"))
        val userAlreadyInLobby = Problem(URI("${baseUrl}user-already-in-lobby"))
        val userIsNotTheHost = Problem(URI("${baseUrl}user-already-in-lobby"))
        val invalidMove = Problem(URI("${baseUrl}invalid-move"))
        val userAlreadyInGame = Problem(URI("${baseUrl}user-already-in-game"))
        val usernameAlreadyExists = Problem(URI("${baseUrl}username-already-exists"))
        val emailAlreadyExists = Problem(URI("${baseUrl}email-already-exists"))
        val userNotFound = Problem(URI("${baseUrl}user-not-found"))
        val gameVariantNotExists = Problem(URI("${baseUrl}game-variant-not-exists"))
        val logoutError = Problem(URI("${baseUrl}logout-error"))
    }
}
