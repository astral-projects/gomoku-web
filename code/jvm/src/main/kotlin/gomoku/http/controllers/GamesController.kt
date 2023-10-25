package gomoku.http.controllers

import gomoku.domain.Id
import gomoku.domain.SystemInfo
import gomoku.domain.errors.InvalidIdError
import gomoku.domain.errors.MakeMoveError
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.model.game.GameDeletedOutputModel
import gomoku.http.model.game.GameExitedOutputModel
import gomoku.http.model.game.GameOutputModel
import gomoku.http.model.game.JoinedGameWithSuccessOutputModel
import gomoku.http.model.game.LobbyExitOutputModel
import gomoku.http.model.game.MoveInputModel
import gomoku.http.model.game.MoveOutputModel
import gomoku.http.model.game.SystemInfoOutputModel
import gomoku.http.model.game.VariantInputModel
import gomoku.services.game.GameCreationError
import gomoku.services.game.GameDeleteError
import gomoku.services.game.GameMakeMoveError
import gomoku.services.game.GamePutError
import gomoku.services.game.GameWaitError
import gomoku.services.game.GamesService
import gomoku.services.game.GettingGameError
import gomoku.services.game.LobbyDeleteError
import gomoku.utils.Failure
import gomoku.utils.NotTested
import gomoku.utils.Success
import gomoku.utils.get
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
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
    /**
     * Retrieves the game with the given id.
     * @param id the id of the game.
     * @return the game with the given id.
     */
    @GetMapping(Uris.Games.GET_BY_ID)
    @NotTested
    fun getGameById(@PathVariable id: Int): ResponseEntity<*> {
        return when (val validId = Id(id)) {
            is Failure -> Problem(
                type = Problem.invalidId,
                title = "Invalid id",
                status = 404,
                detail = "The id must be a positive integer",
                instance = Uris.Games.byId(validId.value.value)
            ).toResponse()

            is Success -> return when (val res = gamesService.getGameById(validId.value)) {
                is Success ->
                    ResponseEntity
                        .status(200)
                        .body(GameOutputModel.serializeFrom(res.value))

                is Failure -> when (res.value) {
                    GettingGameError.GameNotFound -> Problem(
                        type = Problem.gameNotFound,
                        title = "Requested game was not found",
                        status = 404,
                        detail = "The game with id ${validId.value} was not found",
                        instance = Uris.Games.byId(validId.value.value)
                    ).toResponse()
                }
            }
        }
    }

    /**
     * This method is used to find a game,and also if isn't possible to find a match, the user will be added to the Lobby.
     * @param variantInputModel the variant of the game
     * @param user the authenticated user
     * @return a 201 Created response if the game was created successfully
     * If the game variant does not exist, returns a 404 Not Found error.
     * If the user is already in a lobby, returns a 404 Not Found error.
     * If the user is already in a game, returns a 404 Not Found error.
     */
    @PostMapping(Uris.Games.FIND_GAME)
    @NotTested
    fun findGame(
        @Valid @RequestBody
        variantInputModel: VariantInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = user.user.id
        return when (val variant = Id(variantInputModel.id)) {
            is Failure -> when (variant.value) {
                is InvalidIdError.InvalidId -> Problem(
                    type = Problem.invalidId,
                    title = "Invalid id",
                    status = 404,
                    detail = "The id must be a positive integer",
                    instance = Uris.Games.findGame()
                ).toResponse()
            }

            is Success ->
                when (val result = gamesService.findGame(variant.value, userId)) {
                    is Success -> ResponseEntity.status(201).body(result.value)
                    is Failure -> when (result.value) {
                        is GameCreationError.UserAlreadyInGame -> Problem(
                            type = Problem.userAlreadyInGame,
                            title = "User already in game",
                            status = 404,
                            detail = "The user with id <$userId> is already in the game with id <${result.value.gameId}>",
                            instance = Uris.Games.findGame()
                        ).toResponse()
                        is GameCreationError.UserAlreadyInLobby -> Problem(
                            type = Problem.userAlreadyInLobby,
                            title = "User already in lobby",
                            status = 404,
                            detail = "The user with id <$userId> is already in the lobby with id <${result.value.lobbyId}>",
                            instance = Uris.Games.findGame()
                        ).toResponse()
                        GameCreationError.UserAlreadyNotInLobby -> Problem(
                            type = Problem.userAlreadyNotInLobby,
                            title = "User already not in lobby",
                            status = 404,
                            detail = "The user with id <$userId> is already not in a lobby",
                            instance = Uris.Games.findGame()
                        ).toResponse()
                        GameCreationError.VariantNotFound -> Problem(
                            type = Problem.gameVariantNotFound,
                            title = "Game variant not found",
                            status = 404,
                            detail = "The game variant with id <${variant.value}> was not found",
                            instance = Uris.Games.findGame()
                        ).toResponse()
                        GameCreationError.ErrorCreatingGame -> Problem(
                            type = Problem.gameNotFound,
                            title = "Error creating game",
                            status = 404,
                            detail = "The game could not be created try again later",
                            instance = Uris.Games.findGame()
                        ).toResponse()
                    }
                }
        }
    }

    /**
     * Deletes the game with the given id.
     * @param id the id of the game
     * @param user the authenticated user
     * @return a 200-OK response if the game was deleted successfully
     * If the game with the given id does not exist, returns a 404 Not Found error.
     * If the user is not the host of the game, returns a 403 Forbidden error.
     * If the game is already finished, returns a 400 Bad Request error.
     * If the game variant does not exist, returns a 404 Not Found error.
     */
    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    @NotTested
    fun deleteById(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        val userId = user.user.id
        return when (val validId = Id(id)) {
            is Failure -> Problem(
                type = Problem.invalidId,
                title = "Invalid id",
                status = 400,
                detail = "The id must be greater than 0",
                instance = Uris.Games.deleteById(validId.value)
            ).toResponse()

            is Success ->
                when (val game = gamesService.deleteGame(validId.value, userId)) {
                    is Success -> ResponseEntity.status(200).body(GameDeletedOutputModel())
                    is Failure -> when (game.value) {
                        GamePutError.GameNotFound -> Problem(
                            type = Problem.gameNotFound,
                            title = "Game not found",
                            status = 404,
                            detail = "The game with id <$validId> was not found",
                            instance = Uris.Games.deleteById(validId)
                        ).toResponse()

                        GamePutError.UserIsNotTheHost -> Problem(
                            type = Problem.userIsNotTheHost,
                            title = "User is not the host",
                            status = 403,
                            detail = "The user with id <$userId> is not the host of the game with id <$validId>",
                            instance = Uris.Games.deleteById(validId)
                        ).toResponse()

                        GamePutError.GameIsInprogress -> Problem(
                            type = Problem.gameIsInProgress,
                            title = "Game is in progress",
                            status = 400,
                            instance = Uris.Games.deleteById(validId)
                        ).toResponse()
                    }
                }
        }
    }

    /**
     * Returns the system information.
     * @return the system information
     */
    @GetMapping(Uris.Games.GET_SYSTEM_INFO)
    @NotTested
    fun getSystemInfo(): ResponseEntity<SystemInfoOutputModel> {
        val systemInfo: SystemInfo = gamesService.getSystemInfo()
        return ResponseEntity.ok(SystemInfoOutputModel.serializeFrom(systemInfo))
    }

    /**
     * Makes a move in the game with the given id.
     * @param id the id of the game
     * @param play the move to be made
     * @param user the authenticated user
     * @return a 200 OK response if the move was made successfully
     * If the game with the given id does not exist, returns a 404 Not Found error.
     * If the user is not the host of the game, returns a 403 Forbidden error.
     * If the game variant does not exist, returns a 404 Not Found error.
     * If the move is not valid, returns a 400 Bad Request error.
     * If the game is already finished, returns a 400 Bad Request error.
     */
    @PutMapping(Uris.Games.MAKE_MOVE)
    @NotTested
    fun makeMove(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
        @Valid @RequestBody
        play: MoveInputModel,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = user.user.id
        return when (val validId = Id(id)) {
            is Failure -> Problem(
                type = Problem.invalidId,
                title = "Invalid id",
                status = 400,
                detail = "The id must be greater than 0",
                instance = Uris.Games.makeMove(validId.value)
            ).toResponse()

            is Success ->
                when (
                    val responseEntity =
                        gamesService.makeMove(validId.value, userId, Square.toSquare(play.col, play.row))
                ) {
                    is Success -> ResponseEntity.ok(MoveOutputModel())
                    is Failure -> when (responseEntity.value) {
                        is GameMakeMoveError.UserDoesNotBelongToThisGame -> Problem(
                            type = Problem.userIsNotTheHost,
                            title = "User does not belong to this game",
                            status = 403,
                            detail = "The user with id <$userId> is not in the game with id <$id>",
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

                        is GameMakeMoveError.MoveNotValid -> {
                            when (responseEntity.value.error) {
                                is MakeMoveError.GameOver -> Problem(
                                    type = Problem.gameOver,
                                    title = "Game is over",
                                    status = 400,
                                    detail = "The game is already over",
                                    instance = Uris.Games.makeMove(id)
                                ).toResponse()

                                is MakeMoveError.NotYourTurn -> Problem(
                                    type = Problem.notYourNotTurn,
                                    title = "Not your turn",
                                    status = 400,
                                    detail = "Is player <${responseEntity.value.error.player}> turn",
                                    instance = Uris.Games.makeMove(id)
                                ).toResponse()

                                is MakeMoveError.PositionTaken -> {
                                    val col = responseEntity.value.error.square.col.toString()
                                    val row = responseEntity.value.error.square.row.toString()
                                    Problem(
                                        type = Problem.positionTaken,
                                        title = "Position taken",
                                        status = 400,
                                        detail = "The position <$col,$row> already taken",
                                        instance = Uris.Games.makeMove(id)
                                    ).toResponse()
                                }

                                is MakeMoveError.InvalidPosition -> {
                                    val col = responseEntity.value.error.square.col.toString()
                                    val row = responseEntity.value.error.square.row.toString()
                                    Problem(
                                        type = Problem.invalidMove,
                                        title = "Invalid position",
                                        status = 400,
                                        detail = "The position <$col,$row> is invalid",
                                        instance = Uris.Games.makeMove(id)
                                    ).toResponse()
                                }
                            }
                        }
                    }
                }
        }
    }

    /**
     * Exits the game with the given the game id.
     * @param id the id of the game
     * @param user the authenticated user
     * @return a 200-OK response if the game was exited successfully
     * If the game with the given id does not exist, returns a 404 Not Found error.
     * If the user is not the host of the game, returns a 403 Forbidden error.
     * If the game is already finished, returns a 400 Bad Request error.
     * If the game variant does not exist, returns a 404 Not Found error.
     */
    @PostMapping(Uris.Games.EXIT_GAME)
    @NotTested
    fun exitGame(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = user.user.id
        when (val validId = Id(id)) {
            is Failure -> return Problem(
                type = Problem.invalidId,
                title = "Invalid id",
                status = 400,
                detail = "The id must be greater than 0",
                instance = Uris.Games.exitGame(validId.value.toString())
            ).toResponse()

            is Success ->
                return when (val game = gamesService.exitGame(validId.value, userId)) {
                    is Success -> ResponseEntity.status(200).body(GameExitedOutputModel())
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

                        GameDeleteError.GameAlreadyFinished -> Problem(
                            type = Problem.gameAlreadyFinished,
                            title = "Game already finished",
                            status = 400,
                            instance = Uris.Games.exitGame(id)
                        ).toResponse()
                    }
                }
        }
    }

    @GetMapping(Uris.Games.GET_IS_IN_LOBBY)
    @NotTested
    fun waitingInLobby(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        val userId = user.user.id
        when (val validId = Id(id)) {
            is Failure -> return Problem(
                type = Problem.invalidId,
                title = "Invalid id",
                status = 400,
                detail = "The id must be greater than 0",
                instance = Uris.Games.exitGame(validId.value.toString())
            ).toResponse()

            is Success ->
                return when (val res = gamesService.waitForGame(Id(id).get(), user.user.id)) {
                    is Success -> ResponseEntity.status(200).body(JoinedGameWithSuccessOutputModel(res.value))
                    is Failure -> when (res.value) {
                        GameWaitError.UserDoesNotBelongToThisLobby -> Problem(
                            type = Problem.userDoesntBelongToThisGame,
                            title = "User doesnt belong to this lobby",
                            status = 403,
                            detail = "The user with id <$userId> is not in the Lobby with id <$id>",
                            instance = Uris.Games.exitGame(id)
                        ).toResponse()

                        GameWaitError.UserIsInLobby -> Problem(
                            type = Problem.userIsInLobby,
                            title = "User is waiting in lobby",
                            status = 403,
                            detail = "The user with id <$userId> is waiting in lobby with id <$id>. Try again later",
                            instance = Uris.Games.exitGame(id)
                        ).toResponse()
                    }
                }
        }
    }

    @DeleteMapping(Uris.Games.DELETE_IS_IN_LOBBY)
    @NotTested
    fun exitLobby(@PathVariable id: Int, user: AuthenticatedUser): ResponseEntity<*> {
        val userId = user.user.id
        when (val validId = Id(id)) {
            is Failure -> return Problem(
                type = Problem.invalidId,
                title = "Invalid id",
                status = 400,
                detail = "The id must be greater than 0",
                instance = Uris.Games.exitGame(validId.value.toString())
            ).toResponse()

            is Success ->
                return when (val res = gamesService.exitLobby(validId.get(), userId)) {
                    is Success -> ResponseEntity.status(200).body(LobbyExitOutputModel(validId.value.value))
                    is Failure -> {
                        when (res.value) {
                            LobbyDeleteError.LobbyNotFound -> Problem(
                                type = Problem.lobbyNotFound,
                                title = "Lobby not found",
                                status = 404,
                                detail = "The lobby with id <$id> was not found",
                                instance = Uris.Games.exitGame(id)
                            ).toResponse()
                        }
                    }
                }
        }
    }
}
