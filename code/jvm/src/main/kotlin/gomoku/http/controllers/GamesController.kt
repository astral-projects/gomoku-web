package gomoku.http.controllers

import gomoku.http.Uris
import gomoku.services.GamesService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class GamesController(
    private val gamesService: GamesService,
) {

    @GetMapping(Uris.Games.GET_BY_ID)
    fun getById(@PathVariable id: String): ResponseEntity<String> {
        logger.info("GET /games/$id")
        val game = gamesService.getGameById(id.toInt())
        return if (game == null) {
            ResponseEntity.notFound().build()
        } else {
            val x = String.format(
                "Game id: %d, variant: %s, opening rule: %s, board: %s",
                game.game_id,
                game.game_variant,
                game.opening_rule,
                game.board
            )
            ResponseEntity<String>(x, HttpStatus.BAD_GATEWAY) // 302
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
