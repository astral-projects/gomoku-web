package gomoku.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {

    const val PREFIX = "/api"
    const val HOME = PREFIX

    fun home(): URI = URI(HOME)

    object Users {
        const val CREATE = "$PREFIX/users"
        const val TOKEN = "$PREFIX/users/token"
        const val RANKING = "$PREFIX/users/ranking"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val HOME = "$PREFIX/me"
        const val LOGOUT = "$PREFIX/users/logout"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun home(): URI = URI(HOME)
        fun login(): URI = URI(TOKEN)
        fun register(): URI = URI(CREATE)
    }

    object Games {
        const val CREATE = "$PREFIX/games"
        const val GET_BY_ID = "$PREFIX/games/{id}"
        const val DELETE_BY_ID = "$PREFIX/games/{id}"
        const val HOME = "$PREFIX/me"
        const val GET_SYSTEM_INFO = "$PREFIX/info"
        const val MAKE_MOVE = "$PREFIX/games/{id}/move"
        const val EXIT_GAME = "$PREFIX/games/{id}/exit"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun home(): URI = URI(HOME)
        fun create(): URI = URI(CREATE)

        fun deleteById(id: Int) = UriTemplate(GET_BY_ID).expand(id)
    }
}
