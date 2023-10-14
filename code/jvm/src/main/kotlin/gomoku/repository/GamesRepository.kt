package gomoku.repository

import gomoku.domain.game.Game

interface GamesRepository {
    fun getGameById(id: Int): Game?
    fun createGame(game: Game): Game
    fun deleteGame(game: Game)
    // TODO:
    // fun getGamesByPlayerId(playerId: Int): List<Game>
    // fun getGames(limit: Int, offset: Int): List<Game>
}
