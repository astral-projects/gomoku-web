package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.findPlayer
import gomoku.domain.game.board.moves.move.toSquare
import gomoku.http.Uris
import gomoku.http.model.game.MoveInputModel
import gomoku.http.model.game.AuthorOutputModel
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.http.model.game.VariantInputModel
import gomoku.services.game.GamesService
import gomoku.services.user.UsersService
import gomoku.utils.getRidBearer
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GamesController(
    private val gamesService: GamesService,
    private val usersService: UsersService,
) {

    @GetMapping(Uris.Games.GET_BY_ID)
    // TODO(id should be Id and not String, make a interceptor to convert it)
    fun getById(@PathVariable id: String): ResponseEntity<Game> {
        logger.info("GET ${Uris.Games.GET_BY_ID}")
        val game = gamesService.getGameById(Id(id.toInt()))
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
    fun startGame(@RequestBody variantInputModel: VariantInputModel, request: HttpServletRequest): ResponseEntity<String> {
        logger.info("POST ${Uris.Games.START_GAME}")
        val token = getRidBearer(request.getHeader("Authorization"))
        val user = usersService.getUserByToken(token) ?: return ResponseEntity.status(401).body("Invalid Token")
        val res = gamesService.startGame(Id(variantInputModel.id), user)
        return if (res) {
            ResponseEntity.status(201)
                /*.header(
                    "Location",
                    Uris.Games.byId(res).toASCIIString()*/
                .body("Joined the lobby, waiting for an opponent")
        } else {
            // TODO("revisited this code")
            ResponseEntity.status(200).body("Game was created")
        }
    }

    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    fun deleteById(@PathVariable id: String): ResponseEntity<String> {
        logger.info("DELETE ${Uris.Games.DELETE_BY_ID}")
        val game = gamesService.getGameById(Id(id.toInt()))
        return if (game == null) {
            ResponseEntity.notFound().build()
        } else {
            gamesService.deleteGame(game)
            ResponseEntity.ok("Game deleted")
        }
    }

    @GetMapping(Uris.Games.GET_SYSTEM_INFO)
    fun getSystemInfo(): ResponseEntity<SystemInfoOutputModel> {
        logger.info("GET ${Uris.Games.GET_SYSTEM_INFO}")
        val systemInfo: SystemInfo = gamesService.getSystemInfo()
        return ResponseEntity.ok(
            SystemInfoOutputModel(
                systemInfo.GAME_NAME,
                systemInfo.authors.map { AuthorOutputModel(it.firstName, it.lastName, it.gitHubUrl) },
                systemInfo.VERSION,
                systemInfo.releaseDate
            )
        )
    }

    @PutMapping(Uris.Games.MAKE_MOVE)
    fun makeMove(@PathVariable id: Int, @RequestBody move: MoveInputModel, request: HttpServletRequest
    ): ResponseEntity<String> {
        logger.info("PUT ${Uris.Games.MAKE_MOVE}")
        val user = usersService.getUserByToken(getRidBearer(request.getHeader("Authorization"))) ?: return ResponseEntity.status(401).body("Invalid Token")
        val pl= requireNotNull( findPlayer(move.move)){ return ResponseEntity.status(400).body("Your movement is not correct")}
        val responseEntity = gamesService.makeMove(Id(id),user, toSquare(move.move) , pl)
        return ResponseEntity.status(responseEntity.status).body(responseEntity.reasonException)

    }

    // TODO("why are we using HttpServletRequest?")
    @PostMapping(Uris.Games.EXIT_GAME)
    fun exitGame(@PathVariable id: Int, request: HttpServletRequest): ResponseEntity<String> {
        logger.info("POST ${Uris.Games.EXIT_GAME}")
        // TODO("change to interceptor code")
        val token = getRidBearer(request.getHeader("Authorization"))
        val user = usersService.getUserByToken(token) ?: return ResponseEntity.status(401).body("Invalid Token")
        val game = gamesService.exitGame(Id(value = id), user)
        return if (game) {
            ResponseEntity.status(200).body("Game exited")
        } else {
            ResponseEntity.status(403).body("You are not part of this game")
        }
    }

    @GetMapping(Uris.Games.GAME_STATUS)
    fun gameStatus(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<String> {
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
