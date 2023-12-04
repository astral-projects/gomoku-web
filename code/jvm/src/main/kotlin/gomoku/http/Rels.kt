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

    val USER_RECEIPT = LinkRelation("$BASE/user/{id}")

    val GAME_RECEIPT = LinkRelation("$BASE/game/{id}")

    val GAME_VARIANTS_RECEIPT = LinkRelation("$BASE/game/variants")

    val GET_IS_IN_LOBBY_RECEIPT = LinkRelation("$BASE/lobby/{id}")

    val GET_USERS_STATS_RECEIPT = LinkRelation("$BASE/users/stats/search")

    val GET_USER_STATS_RECEIPT = LinkRelation("$BASE/users/{id}/stats")
}
