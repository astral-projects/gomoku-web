package gomoku.repository

import gomoku.domain.game.Game
import gomoku.domain.game.GameId
import gomoku.domain.game.SystemInfo
import gomoku.domain.game.board.Player
import gomoku.domain.game.board.moves.move.Square
import gomoku.domain.user.User
import gomoku.domain.user.UserId

interface GamesRepository {
    fun getGameById(id: Int): Game?
    fun startGame(gameVariant:String, openingRule: String, boardSize: Int, user: Int): Int?
    fun deleteGame(game: Game):Boolean
    fun userBelongsToTheGame(user:User,gameId: GameId):Boolean
    fun getSystemInfo(): SystemInfo
    fun makeMove(gameId: GameId, userId: User, square: Square, player:Player): Boolean
    fun exitGame(gameId: Int,user: User): Boolean
    fun getGameStatus(gameId: Int,user: User): String?
}
