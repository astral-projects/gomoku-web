package gomoku.http.controllers

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.UserId
import gomoku.http.Uris
import gomoku.http.model.game.GameInputModel
import gomoku.services.GamesService
import gomoku.services.UsersService
import gomoku.utils.getRidBearer
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class GamesController(
    private val gamesService: GamesService,
    private val usersService: UsersService,
) {

    @GetMapping(Uris.Games.GET_BY_ID)
    fun getById(@PathVariable id: String): ResponseEntity<Game> {
        logger.info("GET ${Uris.Games.GET_BY_ID}")
        val game = gamesService.getGameById(id.toInt())
        return if (game == null) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(game)
        }
    }

    /*
    This method is used to the user express his intention to start a game.
     */
    @PostMapping(Uris.Games.START_GAME)
    fun startGame(@RequestBody game: GameInputModel, request: HttpServletRequest): ResponseEntity<String> {
        logger.info("POST ${Uris.Games.START_GAME}")
        val token = getRidBearer(request.getHeader("Authorization"))
        val user = usersService.getUserByToken(token) ?: return ResponseEntity.status(401).body("Invalid Token")

        val res = gamesService.startGame(game.gameVariant, game.openingRule, game.boardSize, user)
        if (res != null) {
            return ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Games.byId(res).toASCIIString()
                ).body("Joined the lobby waiting for an opponent")
        } else {
            return ResponseEntity.status(400).body("Error joining the lobby")
        }
    }

    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    fun deleteById(@PathVariable id: String): ResponseEntity<String> {
        logger.info("DELETE ${Uris.Games.DELETE_BY_ID}")
        val game = gamesService.getGameById(id.toInt())
        return if (game == null) {
            ResponseEntity.notFound().build()
        } else {
            gamesService.deleteGame(game)
            ResponseEntity.ok("Game deleted")
        }
    }

    @GetMapping(Uris.Games.GET_SYSTEM_INFO)
    fun getSystemInfo(): ResponseEntity<String> {
        logger.info("GET ${Uris.Games.GET_SYSTEM_INFO}")
        TODO("Not yet implemented")
    }

    @PutMapping(Uris.Games.MAKE_MOVE)
    fun makeMove(gameId: GameId, userId: UserId, square: Square): ResponseEntity<String> {
        logger.info("PUT ${Uris.Games.MAKE_MOVE}")
        TODO("Not yet implemented")
    }

    @PostMapping(Uris.Games.EXIT_GAME)
    fun exitGame(gameId: GameId): ResponseEntity<String> {
        logger.info("POST ${Uris.Games.EXIT_GAME}")
        TODO("Not yet implemented")
    }

    @GetMapping(Uris.Games.GAME_STATUS)
    fun gameStatus( @PathVariable id: String, request: HttpServletRequest): ResponseEntity<String> {
        logger.info("GET ${Uris.Games.GAME_STATUS}")
        val token = getRidBearer(request.getHeader("Authorization"))
        val user = usersService.getUserByToken(token) ?: return ResponseEntity.status(401).body("Invalid Token")
        val gameStatus = gamesService.getGameStatus(user, id.toInt())
        return if (gameStatus == null) {
            ResponseEntity.status(403).body("You are not part of this game")
        } else {
            ResponseEntity.status(200).body(gameStatus)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
