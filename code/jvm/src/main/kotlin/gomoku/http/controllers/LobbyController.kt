package gomoku.http.controllers

import gomoku.domain.components.Id
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.Uris
import gomoku.http.media.Problem
import gomoku.http.media.siren.sirenResponse
import gomoku.http.model.lobby.LobbyOutputModels
import gomoku.services.game.GameWaitError
import gomoku.services.game.GamesService
import gomoku.services.game.LobbyDeleteError
import gomoku.utils.Failure
import gomoku.utils.Success
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class LobbyController(
    private val lobbyService: GamesService
) {
    companion object {
        val lobbyOutputModels = LobbyOutputModels()
    }

    /**
     * Waits for a game in the lobby with the given id.
     * @param id the id of the lobby.
     * @param user the authenticated user.
     */
    @GetMapping(Uris.Lobby.GET_IS_IN_LOBBY)
    fun waitingInLobby(
        @Valid
        @Range(min = 1)
        @PathVariable
        id: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Lobby.isInLobby(id)
        return when (val lobbyIdResult = Id(id)) {
            is Failure -> Problem.invalidLobbyId(instance)
            is Success -> when (val gameWaitResult = lobbyService.waitForGame(lobbyIdResult.value, user.user.id)) {
                is Failure -> when (gameWaitResult.value) {
                    GameWaitError.UserNotInAnyGameOrLobby -> Problem.userNotInAnyGameOrLobby(
                        userId = userId,
                        lobbyId = lobbyIdResult.value,
                        instance = instance
                    )
                }

                is Success -> ResponseEntity.ok().sirenResponse(
                    lobbyOutputModels.waitingInLobby(gameWaitResult.value)
                )
            }
        }
    }

    /**
     * Exits the lobby with the given id.
     * @param id the id of the lobby.
     * @param user the authenticated user.
     */
    @DeleteMapping(Uris.Lobby.EXIT_LOBBY)
    fun exitLobby(
        @PathVariable id: Int,
        user: AuthenticatedUser
    ): ResponseEntity<*> {
        val userId = user.user.id
        val instance = Uris.Lobby.exitLobby(id)
        return when (val lobbyIdResult = Id(id)) {
            is Failure -> Problem.invalidLobbyId(instance)
            is Success -> when (val lobbyDeleteResult = lobbyService.exitLobby(lobbyIdResult.value, userId)) {
                is Failure -> when (lobbyDeleteResult.value) {
                    LobbyDeleteError.LobbyNotFound -> Problem.lobbyNotFound(
                        lobbyId = lobbyIdResult.value,
                        instance = instance
                    )
                }

                is Success -> ResponseEntity.ok().sirenResponse(
                    lobbyOutputModels.exitLobby(lobbyIdResult.value)
                )
            }
        }
    }
}

