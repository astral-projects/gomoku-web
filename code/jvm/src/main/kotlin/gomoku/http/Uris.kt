package gomoku.http

import org.springframework.web.util.UriTemplate
import java.net.URI

/**
 * URI and URI templates for the REST API.
 */
object Uris {

    const val PREFIX = "/api"

    /**
     * Associated with Users controller.
     */
    object Users {
        const val HOME = "$PREFIX/"
        const val REGISTER = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val STATS_BY_TERM = "$PREFIX/users/stats/search"
        const val STATS = "$PREFIX/users/stats"
        const val STATS_BY_ID = "$PREFIX/users/{id}/stats"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val ME = "$PREFIX/users/home"
        const val LOGOUT = "$PREFIX/users/logout"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun byIdStats(id: Int) = UriTemplate(STATS_BY_ID).expand(id)
        fun login(): URI = URI(TOKEN)
        fun home(): URI = URI(HOME)
        fun stats(page: Int = 1, itemsPerPage: Int = 1): URI = URI("$STATS?page=$page&itemsPerPage=$itemsPerPage")
        fun statsByTerm(term: String, page: Int, itemsPerPage: Int): URI =
            URI("$STATS_BY_TERM?term=$term&page=$page&itemsPerPage=$itemsPerPage")
        fun register(): URI = URI(REGISTER)
        fun logout(): URI = URI(LOGOUT)
    }

    /**
     * Associated with Games controller.
     */
    object Games {
        const val FIND_GAME = "$PREFIX/games"
        const val GET_VARIANTS = "$PREFIX/games/variants"
        const val MAKE_MOVE = "$PREFIX/games/{id}/move"
        const val GET_BY_ID = "$PREFIX/games/{id}"
        const val EXIT_GAME = "$PREFIX/games/{id}/exit"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun makeMove(id: Int) = UriTemplate(MAKE_MOVE).expand(id)
        fun exitGame(id: Int) = UriTemplate(EXIT_GAME).expand(id)
        fun findGame() = URI(FIND_GAME)
        fun getVariants() = URI(GET_VARIANTS)
    }

    /**
     * Associated with Lobbies controller.
     */
    object Lobby {
        const val GET_LOBBY = "$PREFIX/lobby/{id}"
        const val EXIT_LOBBY = "$PREFIX/lobby/{id}/exit"
        fun getLobby(id: Int) = UriTemplate(GET_LOBBY).expand(id)
        fun exitLobby(id: Int) = UriTemplate(EXIT_LOBBY).expand(id)
    }

    /**
     * Associated with System controller.
     */
    object System {
        const val GET_SYSTEM_INFO = "$PREFIX/system"

        fun getSystemInfo() = URI(GET_SYSTEM_INFO)
    }
}
