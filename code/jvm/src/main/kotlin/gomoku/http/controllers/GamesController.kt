package gomoku.http.controllers

import gomoku.domain.game.Game
import gomoku.http.Uris
import gomoku.services.GamesService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
