package gomoku.repository

import gomoku.domain.Id
import gomoku.domain.game.Game
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.repository.jdbi.model.game.JdbiGameJoinVariantModel

interface GamesRepository {
    fun getGameById(id: Id): JdbiGameJoinVariantModel?
    // TODO fun getAllVariants(): List<GameVariant>
    // TODO fun getVariantById(variantId: Id): GameVariant?
    fun startGame(variantId: Id, userId: Id): Boolean
    fun deleteGame(game: Game): Boolean
    fun getSystemInfo(): SystemInfo
    fun userBelongsToTheGame(user: User, gameId: Id): Boolean
    fun makeMove(id: Id, userId: Id, square: Square, player: Player): Boolean
    fun exitGame(id: Id, user: User): Boolean
    fun getGameStatus(gameId: Int, user: User): String?
}
