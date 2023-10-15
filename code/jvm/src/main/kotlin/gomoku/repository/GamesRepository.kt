package gomoku.repository

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.UserId

interface GamesRepository {
    fun getGameById(id: Int): Game?
    fun createGame(gameVariant:String,openingRule: String,boardSize: Int, host:Int, guest:Int): Int?
    fun deleteGame(game: Game):Boolean
    fun getSystemInfo(): SystemInfo
    fun makeMove(gameId: GameId, userId: UserId, square: Square): Boolean
    fun exitGame(gameId: GameId): Boolean

}
