package gomoku.http

object UriTemplates {
    private const val BASE = "/api"

    const val USER = "$BASE/users/{user_id}"
    const val GAME = "$BASE/games/{game_id}"
    const val MAKE_MOVE = "$BASE/games/{game_id}/move"
    const val EXIT_GAME = "$BASE/games/{game_id}/exit"
    const val FIND_GAME = "$BASE/games"
    const val SYSTEM_INFO = "$BASE/system"
    const val LOGIN = "$BASE/users/token"
    const val LOGOUT = "$BASE/users/logout"
    const val REGISTER = "$BASE/users"
    const val VARIANTS = "$BASE/games/variants"
    const val LOBBY = "$BASE/lobby/{lobby_id}"
    const val EXIT_LOBBY = "$BASE/lobbies/{lobby_id}/exit"
    const val USER_STATS = "$BASE/users/{user_id}/stats"
    const val USERS_STATS = "$BASE/users/stats?q={query}{&page,itemPerPage}"
    const val USERS_STATS_BY_TERM = "$BASE/users/stats/search?q={query}{&page,itemPerPage}"
    const val ME = "$BASE/users/home"
}
