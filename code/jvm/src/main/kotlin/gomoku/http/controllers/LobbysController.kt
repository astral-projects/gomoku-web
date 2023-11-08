package gomoku.http.controllers

import gomoku.domain.components.Id
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.model.lobby.LobbyExitOutputModel
import gomoku.services.game.GameWaitError
import gomoku.services.game.GamesService
import gomoku.services.game.LobbyDeleteError
import gomoku.services.game.WaitForGameSuccess
import gomoku.utils.Failure
import gomoku.utils.NotTested
import gomoku.utils.Success
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class LobbysController(
    private val lobbyService: GamesService,
) {

    /**
     * Waits for a game in the lobby with the given id.
     * @param id the id of the lobby.
     * @param user the authenticated user.
     */
    @GetMapping(Uris.Games.GET_IS_IN_LOBBY)
    @RequiresAuthentication
    @NotTested
    fun waitingInLobby(
        @Valid @Range(min = 1)
        @PathVariable id: Int,
        user: AuthenticatedUser,
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Games.exitGame(id)
        return when (val lobbyIdResult = Id(id)) {
            is Failure -> Problem.invalidLobbyId(instance)
            is Success -> when (val result = lobbyService.waitForGame(lobbyIdResult.value, user.user.id)) {
                is Success -> when (result.value) {
                    is WaitForGameSuccess.GameMatch -> ResponseEntity.ok(result.value)
                    is WaitForGameSuccess.WaitingInLobby -> ResponseEntity.ok(result.value)
                }

                is Failure -> when (result.value) {
                    is GameWaitError.UserNotInLobby -> Problem.userNotInLobby(
                        userId = userId,
                        lobbyId = lobbyIdResult.value,
                        instance = instance
                    )

                    GameWaitError.UserNotInAnyGameOrLobby -> Problem.userNotInAnyGameOrLobby(
                        userId = userId,
                        instance = instance
                    )
                }
            }
        }
    }

    /**
     * Exits the lobby with the given id.
     * @param id the id of the lobby.
     * @param user the authenticated user.
     */
    @DeleteMapping(Uris.Games.EXIT_LOBBY)
    @NotTested
    fun exitLobby(
        @PathVariable id: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Games.exitGame(id)
        return when (val lobbyIdResult = Id(id)) {
            is Failure -> Problem.invalidLobbyId(instance)
            is Success -> when (val lobbyDeleteResult = lobbyService.exitLobby(lobbyIdResult.value, userId)) {
                is Success -> ResponseEntity.ok(LobbyExitOutputModel(lobbyIdResult.value.value))
                is Failure -> when (lobbyDeleteResult.value) {
                    LobbyDeleteError.LobbyNotFound -> Problem.lobbyNotFound(
                        lobbyId = lobbyIdResult.value,
                        instance = instance
                    )
                }
            }
        }
    }
}