package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.findPlayer
import gomoku.domain.game.board.moves.move.toSquare
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.Uris
import gomoku.http.model.Problem
import gomoku.http.model.game.MoveInputModel
import gomoku.http.model.game.AuthorOutputModel
import gomoku.http.model.game.GameOutputModel
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.http.model.game.VariantInputModel
import gomoku.services.game.GameCreationError
import gomoku.services.game.GamesService
import gomoku.services.game.GettingGameError
import gomoku.services.user.UsersService
import gomoku.utils.Failure
import gomoku.utils.Success
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
    // TODO(id should be Id and not String, make a ArgumentsResolver to convert it)
    fun getById(@PathVariable id: String, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("GET ${Uris.Games.GET_BY_ID}")
        val res = gamesService.getGameById(Id(id.toInt()))
       return when (res){
            is Success -> ResponseEntity
                .status(200)
                .body(GameOutputModel.serializeFrom(res.value))
            is Failure -> when (res.value) {
                GettingGameError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
            }
        }
    }

    /*
    This method is used to the user express his intention to start a game.
     */
    @PostMapping(Uris.Games.START_GAME)
    fun startGame(@RequestBody variantInputModel: VariantInputModel, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Games.START_GAME}")
        val res = gamesService.startGame(Id(variantInputModel.id), user.user)
        return when(res){
            is Success -> ResponseEntity.status(201).body("Game was created")
            is Failure -> when(res.value) {
                GameCreationError.UserAlreadyInLobby -> Problem.response(404, Problem.userAlreadyInLobby)
                GameCreationError.GameNotFound -> TODO()
            }
        }
    }

    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    fun deleteById(@PathVariable id: String, user: AuthenticatedUser): ResponseEntity<String> {
        logger.info("DELETE ${Uris.Games.DELETE_BY_ID}")
        val game = gamesService.getGameById(Id(id.toInt()))
        return when(game) {
            is Success -> {
                ResponseEntity.status(403).body("You are not the host of this game")
            }

            is Failure -> {
                when (game.value) {
                    GettingGameError.GameNotFound -> ResponseEntity.status(404).body("Game not found")
                }
            }
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
    fun makeMove(
        @PathVariable id: Int, @RequestBody move: MoveInputModel, user: AuthenticatedUser
    ): ResponseEntity<String> {
        logger.info("PUT ${Uris.Games.MAKE_MOVE}")
        val pl = requireNotNull(findPlayer(move.move)) {
            return ResponseEntity.status(400).body("Your movement is not correct")
        }
        val responseEntity = gamesService.makeMove(Id(id), user.user, toSquare(move.move), pl)
        return ResponseEntity.status(responseEntity.status).body(responseEntity.reasonException)

    }

    @PostMapping(Uris.Games.EXIT_GAME)
    fun exitGame(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<String> {
        logger.info("POST ${Uris.Games.EXIT_GAME}")
        val game = gamesService.exitGame(Id(value = id), user.user)
        return if (game) {
            ResponseEntity.status(200).body("Game exited")
        } else {
            ResponseEntity.status(403).body("You are not part of this game")
        }
    }

    @GetMapping(Uris.Games.GAME_STATUS)
    fun gameStatus(@PathVariable id: String, user: AuthenticatedUser): ResponseEntity<String> {
        logger.info("GET ${Uris.Games.GAME_STATUS}")
        val gameStatus = gamesService.getGameStatus(user.user, id.toInt())
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
