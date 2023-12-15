package gomoku.http

import gomoku.http.media.siren.LinkRelation

object Rels {

    private const val BASE = "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels"

    val SELF = LinkRelation("self")

    val NEXT = LinkRelation("next")

    val PREV = LinkRelation("prev")

    val FIRST = LinkRelation("first")

    val LAST = LinkRelation("last")

    val USER = LinkRelation("$BASE/user")

    val GAME = LinkRelation("$BASE/game")

    val SYSTEM_INFO = LinkRelation("$BASE/system-info")

    val SYSTEM_INFO_RECIPE = LinkRelation("$BASE/system")

    val USER_STATS = LinkRelation("$BASE/user-stats")

    val USERS_STATS = LinkRelation("$BASE/users-stats")

    val USER_RECIPE = LinkRelation("$BASE/users/{user_id}")

    val GAME_RECIPE = LinkRelation("$BASE/games/{game_id}")

    val GAME_VARIANTS_RECIPE = LinkRelation("$BASE/games/variants")

    val GET_IS_IN_LOBBY_RECIPE = LinkRelation("$BASE/lobbies/{lobby_id}")

    val GET_USERS_STATS_BY_TERM_RECIPE = LinkRelation("$BASE/users/stats/search?q={query}{&page,itemPerPage}")

    val GET_USER_STATS_BY_ID_RECIPE = LinkRelation("$BASE/users/{user_id}/stats")

    val GET_USERS_STATS_RECIPE = LinkRelation("$BASE/users/stats?q={query}{&page,itemPerPage}")

    val REGISTER_RECIPE = LinkRelation("$BASE/register")

    val LOGIN_RECIPE = LinkRelation("$BASE/login")

    val LOGOUT_RECIPE = LinkRelation("$BASE/users/logout")

    val FIND_GAME_RECIPE = LinkRelation("$BASE/games")

    val ME_RECIPE = LinkRelation("$BASE/users/home")
}
