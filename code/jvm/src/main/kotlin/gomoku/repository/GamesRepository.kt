package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User

interface GamesRepository {
    fun getGameById(id: Id): Game?
    // TODO fun getAllVariants(): List<GameVariant>
    // TODO fun getVariantById(variantId: Id): GameVariant?
    fun startGame(variantId: Id, userId: Id): Boolean
    fun deleteGame(game: Game): Boolean
    fun getSystemInfo(): SystemInfo
    fun makeMove(id: Id, userId: Id, square: Square): Boolean
    fun exitGame(id: Id, user: User): Boolean
    fun getGameStatus(gameId: Int, user: User): String?
}
