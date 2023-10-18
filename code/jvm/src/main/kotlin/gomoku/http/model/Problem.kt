package gomoku.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI
) {
    val type = typeUri.toASCIIString()

    companion object {
        const val MEDIA_TYPE = "application/problem+json"
        fun response(status: Int, problem: Problem) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        // TODO("change all uris here to the correct ones and add more")
        val userAlreadyExists = Problem(
            URI(
                "https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/" +
                        "docs/problems/user-already-exists"
            )
        )
        val insecurePassword = Problem(
            URI(
                "https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/" +
                        "docs/problems/insecure-password"
            )
        )

        val userOrPasswordAreInvalid = Problem(
            URI(
                "https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/" +
                        "docs/problems/user-or-password-are-invalid"
            )
        )

        val invalidRequestContent = Problem(
            URI(
                "https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/" +
                        "docs/problems/invalid-request-content"
            )
        )

        val gameNotFound = Problem(
            URI("https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/docs/problems/game-not-found")
        )

        val userAlreadyInLobby = Problem(
            URI("https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/docs/problems/user-already-in-lobby")
        )

        val userIsNotTheHost= Problem(
            URI("https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/docs/problems/user-already-in-lobby")
        )

        val invalidMove= Problem(
            URI("https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/docs/problems/invalid-move")
        )

        val userAlreadyInGame = Problem(
            URI(
                "https://github.com/isel-leic-daw/s2223i-51d-51n-public/tree/main/code/tic-tac-tow-service/docs/problems/user-already-in-game"
            )
        )

    }
}