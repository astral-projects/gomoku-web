package gomoku.http.controllers

import gomoku.domain.components.Id
import gomoku.domain.components.IdError
import gomoku.domain.game.board.errors.MakeMoveError
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.game.board.moves.square.Column
import gomoku.domain.game.board.moves.square.Row
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.model.game.GameDeleteOutputModel
import gomoku.http.model.game.GameExitOutputModel
import gomoku.http.model.game.GameMoveOutputModel
import gomoku.http.model.game.GameOutputModel
import gomoku.http.model.game.MoveInputModel
import gomoku.http.model.game.VariantInputModel
import gomoku.services.game.FindGameSuccess
import gomoku.services.game.GameCreationError
import gomoku.services.game.GameDeleteError
import gomoku.services.game.GameMakeMoveError
import gomoku.services.game.GameUpdateError
import gomoku.services.game.GamesService
import gomoku.services.game.GettingGameError
import gomoku.utils.Failure
import gomoku.utils.NotTested
import gomoku.utils.Success
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
import org.springframework.http.HttpStatus
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
) {

    /**
     * Creates a new game or joins a lobby with the given variant.
     * @param variant the variant of the game.
     * @param user the authenticated user.
     */
    @PostMapping(Uris.Games.FIND_GAME)
    @RequiresAuthentication
    fun findGame(
        @RequestBody
        variant: VariantInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Games.findGame()
        return when (val variantIdResult = Id(variant.variantId)) {
            is Failure -> when (variantIdResult.value) {
                is IdError.InvalidIdError -> Problem.invalidVariantId(instance)
            }

            is Success -> when (val gameCreationResult = gamesService.findGame(variantIdResult.value, userId)) {
                is Success -> when (gameCreationResult.value) {
                    is FindGameSuccess.LobbyCreated ->
                        ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(gameCreationResult.value)

                    is FindGameSuccess.GameMatch ->
                        ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(gameCreationResult.value)
                }

                is Failure -> when (gameCreationResult.value) {
                    is GameCreationError.UserAlreadyLeftTheLobby -> Problem.userNotInLobby(
                        userId = userId,
                        instance = instance,
                        lobbyId = gameCreationResult.value.lobbyId
                    )

                    GameCreationError.VariantNotFound -> Problem.variantNotFound(
                        variantId = variantIdResult.value,
                        instance = instance
                    )

                    GameCreationError.LobbyNotFound -> Problem.lobbyNotFound(
                        instance = instance
                    )

                    GameCreationError.GameInsertionError -> Problem.gameInsertFailure(
                        instance = instance
                    )
                }
            }
        }
    }

    /**
     * Retrieves the game with the given id.
     * @param id the id of the game.
     */
    @GetMapping(Uris.Games.GET_BY_ID)
    fun getGameById(
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val instance = Uris.Games.byId(id)
        return when (val gameIdResult = Id(id)) {
            is Failure -> Problem.invalidGameId(instance)
            is Success -> when (val gameResult = gamesService.getGameById(gameIdResult.value)) {
                is Success -> ResponseEntity.ok(GameOutputModel.serializeFrom(gameResult.value))
                is Failure -> when (gameResult.value) {
                    GettingGameError.GameNotFound -> Problem.gameNotFound(
                        gameId = gameIdResult.value,
                        instance = instance
                    )
                }
            }
        }
    }

    /**
     * Deletes the game with the given id.
     * @param id the id of the game.
     * @param user the authenticated user.
     */
    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    @RequiresAuthentication
    fun deleteById(
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Games.deleteById(id)
        return when (val gameIdResult = Id(id)) {
            is Failure -> Problem.invalidGameId(instance)

            is Success -> when (val game = gamesService.deleteGame(gameIdResult.value, userId)) {
                is Success -> ResponseEntity.ok(GameDeleteOutputModel(gameIdResult.value.value))
                is Failure -> when (game.value) {
                    GameDeleteError.GameNotFound -> Problem.gameNotFound(
                        gameId = gameIdResult.value,
                        instance = instance
                    )

                    GameDeleteError.UserIsNotTheHost -> Problem.userIsNotTheHost(
                        userId = userId,
                        gameId = gameIdResult.value,
                        instance = instance
                    )

                    GameDeleteError.GameIsInProgress -> Problem.gameIsInProgress(
                        gameId = gameIdResult.value,
                        instance = instance
                    )
                }
            }
        }
    }


    /**
     * Exits the game with the given id.
     * @param id the id of the game.
     * @param user the authenticated user.
     */
    @PostMapping(Uris.Games.EXIT_GAME)
    @RequiresAuthentication
    fun exitGame(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Games.exitGame(id)
        return when (val gameIdResult = Id(id)) {
            is Failure -> Problem.invalidGameId(instance)
            is Success -> when (val game = gamesService.exitGame(gameIdResult.value, userId)) {
                is Success -> ResponseEntity.ok(GameExitOutputModel(gameIdResult.value.value))
                is Failure -> when (game.value) {
                    GameUpdateError.GameNotFound -> Problem.gameNotFound(
                        gameId = gameIdResult.value,
                        instance = instance
                    )

                    GameUpdateError.UserNotInGame -> Problem.userDoesntBelongToThisGame(
                        userId = userId,
                        gameId = gameIdResult.value,
                        instance = instance
                    )

                    GameUpdateError.VariantNotFound -> Problem.variantNotFound(instance = instance)

                    GameUpdateError.GameAlreadyFinished -> Problem.gameAlreadyFinished(
                        gameId = gameIdResult.value,
                        instance = instance
                    )
                }
            }
        }
    }

    /**
     * Makes a move in the game with the given id.
     * @param id the game id.
     * @param move the move to be made.
     * @param user the authenticated user.
     */
    @PutMapping(Uris.Games.MAKE_MOVE)
    @RequiresAuthentication
    @NotTested
    fun makeMove(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
        @Valid @RequestBody
        move: MoveInputModel,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Games.makeMove(id)
        return when (val gameIdResult = Id(id)) {
            is Failure -> Problem.invalidGameId(instance)
            is Success -> when (val gettingColumnResult = Column(move.col)) {
                is Failure -> Problem.invalidColumn(instance)
                is Success -> when (val gettingRowResult = Row(move.row)) {
                    is Failure -> Problem.invalidRow(instance)
                    is Success -> {
                        val square = Square(gettingColumnResult.value, gettingRowResult.value)
                        val gameMakeMoveResult =
                            gamesService.makeMove(gameIdResult.value, userId, square)
                        when (gameMakeMoveResult) {
                            is Success -> ResponseEntity.ok(GameMoveOutputModel(id))
                            is Failure -> when (gameMakeMoveResult.value) {
                                is GameMakeMoveError.UserNotInGame -> Problem.userNotInGame(
                                    userId = userId,
                                    gameId = gameIdResult.value,
                                    instance = instance
                                )

                                GameMakeMoveError.GameNotFound -> Problem.gameNotFound(
                                    gameId = gameIdResult.value,
                                    instance = instance
                                )

                                GameMakeMoveError.VariantNotFound -> Problem.variantNotFound(instance = instance)

                                is GameMakeMoveError.MoveNotValid -> when (gameMakeMoveResult.value.error) {
                                    is MakeMoveError.GameOver -> Problem.gameAlreadyFinished(
                                        gameId = gameIdResult.value,
                                        instance = instance
                                    )

                                    is MakeMoveError.NotYourTurn -> Problem.notYourTurn(
                                        gameId = gameIdResult.value,
                                        player = gameMakeMoveResult.value.error.player,
                                        instance = instance
                                    )

                                    is MakeMoveError.PositionTaken -> Problem.positionTaken(
                                        gameId = gameIdResult.value,
                                        col = gameMakeMoveResult.value.error.square.col,
                                        row = gameMakeMoveResult.value.error.square.row,
                                        instance = instance
                                    )

                                    is MakeMoveError.InvalidPosition -> Problem.invalidPosition(
                                        gameId = gameIdResult.value,
                                        col = gameMakeMoveResult.value.error.square.col,
                                        row = gameMakeMoveResult.value.error.square.row,
                                        instance = instance
                                    )
                                }

                                GameMakeMoveError.UserDoesNotBelongToThisGame -> Problem.userDoesntBelongToThisGame(
                                    userId = userId,
                                    gameId = gameIdResult.value,
                                    instance = instance
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the list of available variants.
     */
    @GetMapping(Uris.Games.GET_VARIANTS)
    fun getVariants(): ResponseEntity<*> {
        val variants = gamesService.getVariants()
        return ResponseEntity.ok(variants)
    }

}
