package gomoku.http

import org.springframework.web.util.UriTemplate
import java.net.URI

object Uris {

    const val PREFIX = "/api"
    const val HOME = PREFIX

    fun home(): URI = URI(HOME)

    object Users {
        const val REGISTER = "$PREFIX/users/register"
        const val TOKEN = "$PREFIX/users/token"
        const val LOGOUT = "$PREFIX/logout"
        const val GET_BY_ID = "$PREFIX/users/{id}"
        const val HOME = "$PREFIX/me"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun home(): URI = URI(HOME)
        fun login(): URI = URI(TOKEN)
        fun register(): URI = URI(REGISTER)
    }

    object Games {
        const val CREATE = "$PREFIX/games"
        const val GET_BY_ID = "$PREFIX/games/{id}"
        const val DELETE_BY_ID = "$PREFIX/games/{id}"
        const val HOME = "$PREFIX/me"

        fun byId(id: Int) = UriTemplate(GET_BY_ID).expand(id)
        fun home(): URI = URI(HOME)
        fun create(): URI = URI(CREATE)

        fun deleteById(id: Int) = UriTemplate(GET_BY_ID).expand(id)
    }
}
