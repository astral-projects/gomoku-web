package gomoku.repository

import gomoku.domain.game.Game

interface GamesRepository {
    fun getGameById(id: Int): Game?
    fun createGame(gameVariant:String,openingRule: String,boardSize: Int, host:Int, guest:Int): Int?
    fun deleteGame(game: Game):Boolean
    // TODO:
    // fun getGamesByPlayerId(playerId: Int): List<Game>
    // fun getGames(limit: Int, offset: Int): List<Game>
}
