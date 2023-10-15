package gomoku.http.controllers

import gomoku.domain.game.Game
import gomoku.http.Uris
import gomoku.http.model.Problem
import gomoku.http.model.game.GameInputModel
import gomoku.services.GamesService
import gomoku.services.UserCreationError
import gomoku.utils.Success
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GamesController(
    private val gamesService: GamesService,
) {

    @GetMapping(Uris.Games.GET_BY_ID)
    fun getById(@PathVariable id: String): ResponseEntity<Game> {
        logger.info("GET /games/$id")
        val game = gamesService.getGameById(id.toInt())
        return if (game == null) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(game)
        }
    }

    @PostMapping(Uris.Games.CREATE)
    fun createGame(@RequestBody game: GameInputModel): ResponseEntity<String> {
        val res = gamesService.createGame(game.gameVariant, game.openingRule, game.boardSize, game.host, game.guest)
        if (res != null) {
            return ResponseEntity.status(201)
                .header(
                    "Location",
                    Uris.Games.byId(res).toASCIIString()
                ).body("Game created With Sucess")
        } else {
            return ResponseEntity.status(400).body("Error creating game")
        }
    }

    @DeleteMapping(Uris.Games.DELETE_BY_ID)
    fun deleteById(@PathVariable id: String): ResponseEntity<String> {
        val game = gamesService.getGameById(id.toInt())
        return if (game == null) {
            ResponseEntity.notFound().build()
        } else {
            gamesService.deleteGame(game)
            ResponseEntity.ok("Game deleted")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
