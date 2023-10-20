package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.findPlayer
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.Uris
import gomoku.http.model.Problem
import gomoku.http.model.game.GameOutputModel
import gomoku.http.model.game.MoveInputModel
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.http.model.game.VariantInputModel
import gomoku.services.game.GameCreationError
import gomoku.services.game.GameDeleteError
import gomoku.services.game.GameMakeMoveError
import gomoku.services.game.GamePutError
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
    private val usersService: UsersService
) {

    @GetMapping(Uris.Games.GET_BY_ID)
    fun getById(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("GET ${Uris.Games.GET_BY_ID}")
        val res = gamesService.getGameById(Id(id))
        return when (res) {
            is Success ->
                ResponseEntity
                    .status(200)
                    .body(GameOutputModel.serializeFrom(res.value))

            is Failure -> when (res.value) {
                GettingGameError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
            }
        }
    }

    @PostMapping(Uris.Games.START_GAME)
    fun findGame(@RequestBody variantInputModel: VariantInputModel, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Games.START_GAME}")
        val res = gamesService.findGame(Id(variantInputModel.id), user.user)
        return when (res) {
            is Success -> ResponseEntity.status(201).body(res.value)
            is Failure -> when (res.value) {
                GameCreationError.VariantNotFound -> Problem.response(404, Problem.gameVariantNotExists)
                GameCreationError.UserAlreadyInLobby -> Problem.response(404, Problem.userAlreadyInLobby)
                GameCreationError.UserAlreadyInGame -> Problem.response(404, Problem.userAlreadyInGame)
                GameCreationError.GameNotFound -> TODO()
                GameCreationError.VariantNotFound -> TODO()
            }
        }
    }

    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    fun deleteById(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("DELETE ${Uris.Games.DELETE_BY_ID}")
        val game = gamesService.deleteGame(Id(id), user.user.id)
        return when (game) {
            is Success -> ResponseEntity.status(200).body("Game deleted")
            is Failure -> when (game.value) {
                GamePutError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
                GamePutError.UserIsNotTheHost -> Problem.response(403, Problem.userIsNotTheHost)
            }
        }
    }

    @GetMapping(Uris.Games.GET_SYSTEM_INFO)
    fun getSystemInfo(): ResponseEntity<SystemInfoOutputModel> {
        logger.info("GET ${Uris.Games.GET_SYSTEM_INFO}")
        val systemInfo: SystemInfo = gamesService.getSystemInfo()
        return ResponseEntity.ok(SystemInfoOutputModel.serializeFrom(systemInfo))
    }

    @PutMapping(Uris.Games.MAKE_MOVE)
    fun makeMove(
        @PathVariable id: Int,
        @RequestBody move: MoveInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        logger.info("PUT ${Uris.Games.MAKE_MOVE}")
        val pl = requireNotNull(findPlayer(move.move)) {
            return ResponseEntity.status(400).body("Your movement is not correct")
        }
        val responseEntity = gamesService.makeMove(Id(id), user.user.id, Square.toSquare(move.move), pl)
        return when (responseEntity) {
            is Success -> ResponseEntity.status(200).body("Move made")
            is Failure -> when (responseEntity.value) {
                GameMakeMoveError.UserDoesNotBelongToThisGame -> Problem.response(403, Problem.userIsNotTheHost)
                GameMakeMoveError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
                is GameMakeMoveError.MoveNotValid -> Problem.response(400, Problem.invalidMove)
            }
        }
    }

    @PostMapping(Uris.Games.EXIT_GAME)
    fun exitGame(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Games.EXIT_GAME}")
        val game = gamesService.exitGame(Id(id), user.user.id)
        return when (game) {
            is Success -> ResponseEntity.status(200).body("Game exited")
            is Failure -> when (game.value) {
                GameDeleteError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
                GameDeleteError.UserDoesntBelongToThisGame -> Problem.response(403, Problem.userIsNotTheHost)
            }
        }
    }

    @GetMapping(Uris.Games.GAME_STATUS)
    fun gameStatus(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("GET ${Uris.Games.GAME_STATUS}")
        val gameStatus = gamesService.getGameStatus(user.user.id, Id(id))
        return when (gameStatus) {
            is Success -> ResponseEntity.status(200).body(gameStatus.value.state.toString())
            is Failure -> when (gameStatus.value) {
                GettingGameError.GameNotFound -> Problem.response(404, Problem.gameNotFound)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
