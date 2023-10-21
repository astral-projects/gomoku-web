package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.SystemInfo
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
import gomoku.utils.Failure
import gomoku.utils.NotTested
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
    private val gamesService: GamesService
) {

    @GetMapping(Uris.Games.GET_BY_ID)
    @NotTested
    fun getById(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("GET ${Uris.Games.GET_BY_ID}")
        return when (val res = gamesService.getGameById(Id(id))) {
            is Success ->
                ResponseEntity
                    .status(200)
                    .body(GameOutputModel.serializeFrom(res.value))

            is Failure -> when (res.value) {
                GettingGameError.GameNotFound -> Problem(
                    type = Problem.gameNotFound,
                    title = "Requested game was not found",
                    status = 404,
                    detail = "The game with id $id was not found",
                    instance = Uris.Games.byId(id)
                ).toResponse()
            }
        }
    }

    @PostMapping(Uris.Games.START_GAME)
    @NotTested
    fun findGame(@RequestBody variantInputModel: VariantInputModel, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Games.START_GAME}")
        val userId = user.user.id
        return when (val result = gamesService.findGame(Id(variantInputModel.id), userId)) {
            is Success -> ResponseEntity.status(201).body(result.value)
            is Failure -> when (result.value) {
                GameCreationError.VariantNotFound -> Problem(
                    type = Problem.gameVariantNotFound,
                    title = "Requested game variant not found",
                    status = 404,
                    detail = "The game variant with id <${variantInputModel.id}> was not found",
                    instance = Uris.Games.create()
                ).toResponse()
                GameCreationError.UserAlreadyInLobby -> Problem(
                    type = Problem.userAlreadyInLobby,
                    title = "User already in lobby",
                    status = 404,
                    detail = "The user with id <$userId> is already in a lobby",
                    instance = Uris.Games.create()
                ).toResponse()
                GameCreationError.UserAlreadyInGame -> Problem(
                    type = Problem.userAlreadyInGame,
                    title = "User already in game",
                    status = 404,
                    detail = "The user with id <$userId> is already in game",
                    instance = Uris.Games.create()
                ).toResponse()
                GameCreationError.GameNotFound -> Problem(
                    type = Problem.gameNotFound,
                    title = "Game not found",
                    status = 404,
                    instance = Uris.Games.create()
                ).toResponse()
            }
        }
    }

    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    @NotTested
    fun deleteById(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("DELETE ${Uris.Games.DELETE_BY_ID}")
        val userId = user.user.id
        return when (val game = gamesService.deleteGame(Id(id), userId)) {
            is Success -> ResponseEntity.status(200).body("Game deleted")
            is Failure -> when (game.value) {
                GamePutError.GameNotFound -> Problem(
                    type = Problem.gameNotFound,
                    title = "Game not found",
                    status = 404,
                    detail = "The game with id <$id> was not found",
                    instance = Uris.Games.deleteById(id)
                ).toResponse()
                GamePutError.UserIsNotTheHost -> Problem(
                    type = Problem.userIsNotTheHost,
                    title = "User is not the host",
                    status = 403,
                    detail = "The user with id <$userId> is not the host of the game with id <$id>",
                    instance = Uris.Games.deleteById(id)
                ).toResponse()
            }
        }
    }

    @GetMapping(Uris.Games.GET_SYSTEM_INFO)
    @NotTested
    fun getSystemInfo(): ResponseEntity<SystemInfoOutputModel> {
        logger.info("GET ${Uris.Games.GET_SYSTEM_INFO}")
        val systemInfo: SystemInfo = gamesService.getSystemInfo()
        return ResponseEntity.ok(SystemInfoOutputModel.serializeFrom(systemInfo))
    }

    @PutMapping(Uris.Games.MAKE_MOVE)
    @NotTested
    fun makeMove(
        @PathVariable id: Int,
        @RequestBody move: MoveInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        logger.info("PUT ${Uris.Games.MAKE_MOVE}")
        val player = requireNotNull(findPlayer(move.move)) {
            return ResponseEntity.status(400).body("Your movement is not correct")
        }
        val userId = user.user.id
        val responseEntity = gamesService.makeMove(Id(id), userId, Square.toSquare(move.move), player)
        return when (responseEntity) {
            is Success -> ResponseEntity.status(200).body("The move was performed successfully")
            is Failure -> when (responseEntity.value) {
                GameMakeMoveError.UserDoesNotBelongToThisGame -> Problem(
                    type = Problem.userIsNotTheHost,
                    title = "User is not the host",
                    status = 403,
                    detail = "The user is not the host of the game with id <$userId>",
                    instance = Uris.Games.makeMove(id)
                ).toResponse()
                GameMakeMoveError.GameNotFound -> Problem(
                    type = Problem.gameNotFound,
                    title = "Game not found",
                    status = 404,
                    detail = "The game with id <$id> was not found",
                    instance = Uris.Games.makeMove(id)
                ).toResponse()
                GameMakeMoveError.VariantNotFound -> Problem(
                    type = Problem.gameVariantNotFound,
                    title = "Game variant not found",
                    status = 404,
                    instance = Uris.Games.makeMove(id)
                ).toResponse()
                is GameMakeMoveError.MoveNotValid -> Problem(
                    type = Problem.invalidMove,
                    title = "Invalid move",
                    status = 400,
                    detail = responseEntity.value.error.toString(),
                    instance = Uris.Games.makeMove(id)
                ).toResponse()
            }
        }
    }

    @PostMapping(Uris.Games.EXIT_GAME)
    @NotTested
    fun exitGame(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("POST ${Uris.Games.EXIT_GAME}")
        val userId = user.user.id
        return when (val game = gamesService.exitGame(Id(id), userId)) {
            is Success -> ResponseEntity.status(200).body("Game exited")
            is Failure -> when (game.value) {
                GameDeleteError.GameNotFound -> Problem(
                    type = Problem.gameNotFound,
                    title = "Game not found",
                    status = 404,
                    detail = "The game with id <$id> was not found",
                    instance = Uris.Games.exitGame(id)
                ).toResponse()
                GameDeleteError.UserDoesntBelongToThisGame -> Problem(
                    type = Problem.userIsNotTheHost,
                    title = "User is not the host",
                    status = 403,
                    detail = "The user with id <$userId> is not in the game with id <$id>",
                    instance = Uris.Games.exitGame(id)
                ).toResponse()
                GameDeleteError.VariantNotFound -> Problem(
                    type = Problem.gameVariantNotFound,
                    title = "Game variant not found",
                    status = 404,
                    instance = Uris.Games.exitGame(id)
                ).toResponse()
            }
        }
    }

    @GetMapping(Uris.Games.GAME_STATUS)
    @NotTested
    fun gameStatus(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        logger.info("GET ${Uris.Games.GAME_STATUS}")
        val userId = user.user.id
        return when (val gameStatus = gamesService.getGameStatus(userId, Id(id))) {
            is Success -> ResponseEntity.status(200).body(gameStatus.value.state.toString())
            is Failure -> when (gameStatus.value) {
                GettingGameError.GameNotFound -> Problem(
                    type = Problem.gameNotFound,
                    title = "Game not found",
                    status = 404,
                    detail = "The game with id <$id> was not found",
                    instance = Uris.Games.gameStatus(id)
                ).toResponse()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
