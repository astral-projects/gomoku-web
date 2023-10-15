package gomoku.http.controllers

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.UserId
import gomoku.http.Uris
import gomoku.http.model.game.GameInputModel
import gomoku.services.GamesService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @GetMapping(Uris.Games.GET_SYSTEM_INFO)
    fun getSystemInfo(): ResponseEntity<String> {
        // TODO("Not yet implemented")
        return ResponseEntity.ok("System info")
    }

    @PutMapping(Uris.Games.MAKE_MOVE)
    fun makeMove(gameId: GameId, userId: UserId, square: Square): ResponseEntity<String> {
        // TODO("Not yet implemented")
        return ResponseEntity.ok("Move made")
    }

    @PostMapping(Uris.Games.EXIT_GAME)
    fun exitGame(gameId: GameId): ResponseEntity<String> {
        // TODO("Not yet implemented")
        return ResponseEntity.ok("Game exited")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GamesController::class.java)
    }
}
