package gomoku.http

import gomoku.http.media.siren.LinkRelation

object Rels {

    private const val BASE = "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels"

    val SELF = LinkRelation("self")

    val NEXT = LinkRelation("next")

    val PREV = LinkRelation("prev")

    val FIRST = LinkRelation("first")

    val LAST = LinkRelation("last")

    val SYSTEM = LinkRelation("$BASE/system-info")

    val LOGIN = LinkRelation("$BASE/login")

    val LOGOUT = LinkRelation("$BASE/logout")

    val REGISTER = LinkRelation("$BASE/register")

    val ME = LinkRelation("$BASE/me")

    val USER = LinkRelation("$BASE/users/user")

    val GAMES = LinkRelation("$BASE/find-game")

    val GAME = LinkRelation("$BASE/games/game")

    val MAKE_MOVE = LinkRelation("$BASE/games/game/move")

    val EXIT_GAME = LinkRelation("$BASE/games/game/exit-game")

    val VARIANTS = LinkRelation("$BASE/games/variants")

    val LOBBY = LinkRelation("$BASE/lobbies/lobby")

    val EXIT_LOBBY = LinkRelation("$BASE/lobbies/lobby/exit-lobby")

    val USER_STATS_BY_TERM = LinkRelation("$BASE/users/search")

    val USER_STATS = LinkRelation("$BASE/users/user/stats")

    val USERS_STATS = LinkRelation("$BASE/users/stats")
}
