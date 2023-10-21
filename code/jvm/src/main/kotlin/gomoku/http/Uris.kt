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
        const val RANKING = "$PREFIX/users/ranking"
        const val STATS_BY_ID = "$PREFIX/users/{id}/stats"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val EDIT_USER_PROFILE = "$PREFIX/users/{id}"
        const val HOME = "$PREFIX/home"
        const val LOGOUT = "$PREFIX/logout"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun login(): URI = URI(TOKEN)
        fun register(): URI = URI(REGISTER)
        fun logout(): URI = URI(LOGOUT)
    }

    object Games {
        const val START_GAME = "$PREFIX/games"
        const val GAME_STATUS_BY_ID = "$PREFIX/games/{id}"
        const val DELETE_BY_ID = "$PREFIX/games/{id}"
        const val GET_SYSTEM_INFO = "$PREFIX/system"
        const val MAKE_MOVE = "$PREFIX/games/{id}/move"
        const val EXIT_GAME = "$PREFIX/games/{id}/exit"

        fun byId(id: Int) = UriTemplate(GAME_STATUS_BY_ID).expand(id)
        fun makeMove(id: Int) = UriTemplate(MAKE_MOVE).expand(id)
        fun create(): URI = URI(START_GAME)
        fun deleteById(id: Int) = UriTemplate(GAME_STATUS_BY_ID).expand(id)
        fun exitGame(id: Int) = UriTemplate(EXIT_GAME).expand(id)
    }
}
