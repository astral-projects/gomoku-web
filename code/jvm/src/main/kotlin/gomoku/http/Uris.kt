package gomoku.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {

    const val PREFIX = "/api"
    const val HOME = PREFIX
    fun home(): URI = URI(HOME)

    object Users {
        const val REGISTER = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val STATS = "$PREFIX/users/stats"
        const val STATS_BY_ID = "$PREFIX/users/{id}/stats"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val EDIT_BY_ID = "$PREFIX/users/{id}"
        const val HOME = "$PREFIX/users/home"
        const val LOGOUT = "$PREFIX/users/logout"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun byIdStats(id: Int) = UriTemplate(STATS_BY_ID).expand(id)
        fun login(): URI = URI(TOKEN)
        fun stats(): URI = URI(STATS)
        fun register(): URI = URI(REGISTER)
        fun logout(): URI = URI(LOGOUT)
    }

    object Games {
        const val FIND_GAME = "$PREFIX/games"
        const val FIND_VARIANTS = "$PREFIX/games/variants"
        const val MAKE_MOVE = "$PREFIX/games/{id}/move"
        const val GET_BY_ID = "$PREFIX/games/{id}"
        const val DELETE_BY_ID = "$PREFIX/games/{id}"
        const val GET_SYSTEM_INFO = "$PREFIX/system"
        const val EXIT_GAME = "$PREFIX/games/{id}/exit"

        // TODO("move to another controller")
        const val GET_IS_IN_LOBBY = "$PREFIX/games/lobby/{id}"
        const val DELETE_IS_IN_LOBBY = "$PREFIX/games/lobby/{id}"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun makeMove(id: Any) = UriTemplate(MAKE_MOVE).expand(id)
        fun deleteById(id: Any) = UriTemplate(GET_BY_ID).expand(id)
        fun exitGame(id: Any) = UriTemplate(EXIT_GAME).expand(id)
        fun findGame() = URI(FIND_GAME)

        fun findVariants() = URI(FIND_VARIANTS)
    }
}
