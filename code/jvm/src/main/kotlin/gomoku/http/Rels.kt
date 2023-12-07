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

    val USER_STATS = LinkRelation("$BASE/user-stats")

    val USERS_STATS = LinkRelation("$BASE/users-stats")

    val USER_RECIPE = LinkRelation("$BASE/users/{/user_id}")

    val GAME_RECIPE = LinkRelation("$BASE/games/{/game_id}")

    val GAME_VARIANTS_RECIPE = LinkRelation("$BASE/games/variants")

    val GET_IS_IN_LOBBY_RECIPE = LinkRelation("$BASE/lobbies/{/lobby_id}")

    val GET_USERS_STATS_RECIPE = LinkRelation("$BASE/users/stats/search")

    val GET_USER_STATS_RECIPE = LinkRelation("$BASE/users/{/user_id}/stats")

    val REGISTER_RECIPE = LinkRelation("$BASE/users")

    val LOGIN_RECIPE = LinkRelation("$BASE/users/token")

    val LOGOUT_RECIPE = LinkRelation("$BASE/users/logout")

    val FIND_GAME_RECIPE = LinkRelation("$BASE/games")
}
