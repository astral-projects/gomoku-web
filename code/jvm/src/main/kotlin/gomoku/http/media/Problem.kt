package gomoku.http.media

import gomoku.domain.components.Id
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.user.components.Email
import gomoku.domain.user.components.Username
import org.springframework.http.ResponseEntity
import java.net.URI

/**
 * Represents a problem that occurred while processing a request.
 * @param type A URI reference that identifies the problem type.
 * @param title A short, human-readable summary of the problem type.
 * @param status The HTTP status code generated by the origin server for this occurrence of the problem.
 * @param detail A human-readable explanation specific to this occurrence of the problem.
 * @param instance A URI reference that identifies the specific occurrence of the problem.
 * @param data Additional information about the problem.
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 */
data class Problem(
    val type: URI,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: URI? = null,
    val data: Map<String, Any>? = null
) {
    fun toResponse() = ResponseEntity
        .status(status)
        .header("Content-Type", MEDIA_TYPE)
        .header("Content-Language", "en")
        .body<Any>(this)

    companion object {

        private const val BASE_URL = "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/problems/"
        const val MEDIA_TYPE = "application/problem+json"

        val invalidRequestContent = URI("${BASE_URL}invalid-request-content")
        private val insecurePassword = URI("${BASE_URL}insecure-password")
        private val gameNotFound = URI("${BASE_URL}game-not-found")
        private val userIsNotTheHost = URI("${BASE_URL}user-is-not-the-host")
        private val userNotInGame = URI("${BASE_URL}user-not-in-game")
        private val invalidMove = URI("${BASE_URL}invalid-move")
        private val positionTaken = URI("${BASE_URL}position-taken")
        private val userAlreadyInGame = URI("${BASE_URL}user-already-in-game")
        private val usernameAlreadyExists = URI("${BASE_URL}username-already-exists")
        private val emailAlreadyExists = URI("${BASE_URL}email-already-exists")
        private val userNotFound = URI("${BASE_URL}user-not-found")
        private val gameVariantNotFound = URI("${BASE_URL}game-variant-not-found")
        private val tokenIsInvalid = URI("${BASE_URL}token-is-invalid")
        private val gameAlreadyFinished = URI("${BASE_URL}game-already-finished")
        private val gameIsInProgress = URI("${BASE_URL}game-is-in-progress")
        private val invalidId = URI("${BASE_URL}invalid-id")
        private val invalidOffset = URI("${BASE_URL}invalid-offset")
        private val invalidLimit = URI("${BASE_URL}invalid-limit")
        private val invalidEmail = URI("${BASE_URL}invalid-email")
        private val blankUsername = URI("${BASE_URL}blank-username")
        private val passwordIsEmpty = URI("${BASE_URL}password-is-empty")
        private val invalidUsernameLength = URI("${BASE_URL}invalid-username-length")
        private val notYourTurn = URI("${BASE_URL}not-your-turn")
        private val passwordIsWrong = URI("${BASE_URL}password-is-wrong")
        private val userNotInLobby = URI("${BASE_URL}user-not-in-lobby")
        private val userDoesntBelongToThisGame = URI("${BASE_URL}user-doesnt-belong-to-this-game")
        private val lobbyNotFound = URI("${BASE_URL}lobby-not-found")
        private val userDoesntBelongToAnyGameOrLobby = URI("${BASE_URL}user-doesnt-belong-to-any-game-or-lobby")
        private val invalidRow = URI("${BASE_URL}invalid-row")
        private val invalidColumn = URI("${BASE_URL}invalid-column")
        private val gameInsertFailure = URI("${BASE_URL}game-insert-failure")
        private val emptyUsername = URI("${BASE_URL}empty-username")
        private val usernameDoesNotExists = URI("${BASE_URL}username-doesnt-exists")

        private fun invalidId(idType: String, instance: URI): ResponseEntity<*> = Problem(
            type = invalidId,
            title = "Invalid $idType id",
            status = 400,
            detail = "The $idType id must be a positive integer",
            instance = instance
        ).toResponse()

        fun invalidGameId(instance: URI): ResponseEntity<*> = invalidId("game", instance)
        fun invalidUserId(instance: URI): ResponseEntity<*> = invalidId("user", instance)
        fun invalidVariantId(instance: URI): ResponseEntity<*> = invalidId("variant", instance)
        fun invalidLobbyId(instance: URI): ResponseEntity<*> = invalidId("lobby", instance)

        fun gameNotFound(gameId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = gameNotFound,
            title = "Requested game was not found",
            status = 404,
            detail = "The game with id <${gameId.value}> was not found",
            instance = instance
        ).toResponse()

        fun userAlreadyInGame(userId: Id, gameId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = userAlreadyInGame,
            title = "User already in game",
            status = 400,
            detail = "The user with id <${userId.value}> is already in a game.",
            instance = instance,
            data = mapOf(
                "userId" to userId.value,
                "gameId" to gameId.value
            )
        ).toResponse()

        fun userNotInLobby(userId: Id, lobbyId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = userNotInLobby,
            title = "User not in lobby",
            status = 404,
            detail = "The user with id <$userId> is not in a lobby",
            instance = instance,
            data = mapOf(
                "userId" to userId.value,
                "lobbyId" to lobbyId.value
            )
        ).toResponse()

        fun variantNotFound(variantId: Id? = null, instance: URI): ResponseEntity<*> =
            if (variantId == null) {
                Problem(
                    type = gameVariantNotFound,
                    title = "Game variant not found",
                    status = 404,
                    detail = "The game variant was not found",
                    instance = instance
                ).toResponse()
            } else {
                Problem(
                    type = gameVariantNotFound,
                    title = "Game variant not found",
                    status = 404,
                    detail = "The game variant with id <${variantId.value}> was not found",
                    instance = instance
                ).toResponse()
            }

        fun gameInsertFailure(instance: URI): ResponseEntity<*> = Problem(
            type = gameInsertFailure,
            title = "Error creating game",
            status = 409,
            detail = "The game could not be created try again later",
            instance = instance
        ).toResponse()

        fun userIsNotTheHost(userId: Id, gameId: Id, instance: URI) = Problem(
            type = userIsNotTheHost,
            title = "User is not the host",
            status = 400,
            detail = "The user with id <${userId.value}> is not the host of the game.",
            instance = instance,
            data = mapOf(
                "userId" to userId.value,
                "gameId" to gameId.value
            )
        ).toResponse()

        fun invalidColumn(instance: URI): ResponseEntity<*> = Problem(
            type = invalidColumn,
            title = "Invalid column",
            status = 400,
            detail = "The column must be a letter between a and z",
            instance = instance
        ).toResponse()

        fun invalidRow(instance: URI): ResponseEntity<*> = Problem(
            type = invalidRow,
            title = "Invalid row",
            status = 400,
            detail = "The row must be a positive integer",
            instance = instance
        ).toResponse()

        fun lobbyNotFound(lobbyId: Id? = null, instance: URI): ResponseEntity<*> {
            val detail = if (lobbyId == null) {
                "The lobby was not found"
            } else {
                "The lobby with id <${lobbyId.value}> was not found"
            }
            return Problem(
                type = lobbyNotFound,
                title = "Requested lobby was not found",
                status = 404,
                detail = detail,
                instance = instance
            ).toResponse()
        }

        fun blankUsername(instance: URI): ResponseEntity<*> = Problem(
            type = blankUsername,
            title = "Blank username",
            status = 400,
            detail = "The username cannot be blank",
            instance = instance
        ).toResponse()

        fun emptyUsername(instance: URI): ResponseEntity<*> = Problem(
            type = emptyUsername,
            title = "Empty username",
            status = 400,
            detail = "The username cannot be empty",
            instance = instance
        ).toResponse()

        fun blankPassword(instance: URI): ResponseEntity<*> = Problem(
            type = passwordIsEmpty,
            title = "Blank password",
            status = 400,
            detail = "The password cannot be blank",
            instance = instance
        ).toResponse()

        fun emptyPassword(instance: URI): ResponseEntity<*> = Problem(
            type = passwordIsEmpty,
            title = "Empty password",
            status = 400,
            detail = "The password cannot be empty",
            instance = instance
        ).toResponse()

        fun userNotFound(userId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = userNotFound,
            title = "User not found",
            status = 404,
            detail = "The user with id <${userId.value}> was not found",
            instance = instance
        ).toResponse()

        fun invalidLimit(instance: URI): ResponseEntity<*> = Problem(
            type = invalidLimit,
            title = "Invalid limit",
            status = 400,
            detail = "The limit must be a positive integer",
            instance = instance
        ).toResponse()

        fun invalidOffset(instance: URI): ResponseEntity<*> = Problem(
            type = invalidOffset,
            title = "Invalid offset",
            status = 400,
            detail = "The offset must be a non-negative integer",
            instance = instance
        ).toResponse()

        fun invalidToken(instance: URI): ResponseEntity<*> = Problem(
            type = tokenIsInvalid,
            title = "Received token is invalid",
            status = 400,
            detail = "The received token is invalid because it does not correspond to any active user, or " +
                "it has already been revoked",
            instance = instance
        ).toResponse()

        fun usernameAlreadyExists(username: Username, instance: URI): ResponseEntity<*> = Problem(
            type = usernameAlreadyExists,
            title = "Username already exists",
            status = 400,
            detail = "The username <${username.value}> already exists",
            instance = instance
        ).toResponse()

        fun passwordIsWrong(instance: URI): ResponseEntity<*> = Problem(
            type = passwordIsWrong,
            title = "Wrong password",
            status = 400,
            detail = "The password received does not match the user's username",
            instance = instance
        ).toResponse()

        fun insecurePassword(instance: URI): ResponseEntity<*> = Problem(
            type = insecurePassword,
            title = "Password not safe",
            status = 400,
            detail = "Password must be between 8 and 40 characters",
            instance = instance
        ).toResponse()

        fun invalidUsernameLength(instance: URI): ResponseEntity<*> = Problem(
            type = invalidUsernameLength,
            title = "Username invalid",
            status = 400,
            detail = "Username must be between 5 and 30 characters",
            instance = instance
        ).toResponse()

        fun emailAlreadyExists(email: Email, instance: URI): ResponseEntity<*> = Problem(
            type = emailAlreadyExists,
            title = "Email already exists",
            status = 400,
            detail = "The email <${email.value}> already exists",
            instance = instance
        ).toResponse()

        fun invalidEmail(instance: URI): ResponseEntity<*> = Problem(
            type = invalidEmail,
            title = "Invalid email",
            status = 400,
            detail = "The email is invalid because it does not match the email format",
            instance = instance,
            data = mapOf(
                "regex" to Email.EMAIL_FORMAT
            )
        ).toResponse()

        fun gameIsInProgress(gameId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = gameIsInProgress,
            title = "Game is in progress",
            status = 400,
            detail = "The game with id <${gameId.value}> is in progress",
            instance = instance,
            data = mapOf(
                "gameId" to gameId.value
            )
        ).toResponse()

        fun userNotInGame(userId: Id, gameId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = userNotInGame,
            title = "User not in game",
            status = 404,
            detail = "The user with id <${userId.value}> is not in the game.",
            instance = instance,
            data = mapOf(
                "userId" to userId.value,
                "gameId" to gameId.value
            )
        ).toResponse()

        fun notYourTurn(gameId: Id, player: Player, instance: URI): ResponseEntity<*> = Problem(
            type = notYourTurn,
            title = "Not your turn",
            status = 400,
            detail = "Turn belongs to: ${player.name}",
            instance = instance,
            data = mapOf(
                "gameId" to gameId.value,
                "player" to player.name
            )
        ).toResponse()

        fun positionTaken(gameId: Id, col: Column, row: Row, instance: URI): ResponseEntity<*> = Problem(
            type = positionTaken,
            title = "Position taken",
            status = 400,
            detail = "The position <$col, $row> is already taken",
            instance = instance,
            data = mapOf(
                "gameId" to gameId.value
            )
        ).toResponse()

        fun invalidPosition(gameId: Id, col: Column, row: Row, instance: URI): ResponseEntity<*> = Problem(
            type = invalidMove,
            title = "Invalid position",
            status = 400,
            detail = "The position <$col, $row> is invalid",
            instance = instance,
            data = mapOf(
                "gameId" to gameId.value
            )
        ).toResponse()

        fun userDoesntBelongToThisGame(userId: Id, gameId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = userDoesntBelongToThisGame,
            title = "User doesn't belong to this game",
            status = 400,
            detail = "The user with id <${userId.value}> doesn't belong to the game.",
            instance = instance,
            data = mapOf(
                "userId" to userId.value,
                "gameId" to gameId.value
            )
        ).toResponse()

        fun gameAlreadyFinished(gameId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = gameAlreadyFinished,
            title = "Game already finished",
            status = 400,
            detail = "The game with id <${gameId.value}> is already finished",
            instance = instance,
            data = mapOf(
                "gameId" to gameId.value
            )
        ).toResponse()

        fun userNotInAnyGameOrLobby(userId: Id, instance: URI): ResponseEntity<*> = Problem(
            type = userDoesntBelongToAnyGameOrLobby,
            title = "User doesn't belong to any game or lobby",
            status = 403,
            detail = "The user with id <${userId.value}> doesn't belong to any game or lobby",
            instance = instance
        ).toResponse()

        fun usernameDoesNotExist(username: Username, instance: URI): ResponseEntity<*> = Problem(
            type = usernameDoesNotExists,
            title = "Username doesn't exist",
            status = 404,
            detail = "The user with username <${username.value}> does not exist",
            instance = instance
        ).toResponse()
    }
}
