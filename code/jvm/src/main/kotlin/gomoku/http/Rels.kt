package gomoku.http

import gomoku.http.media.siren.LinkRelation

object Rels {

    private const val BASE_URL = "https://github.com/isel-leic-daw/2023-daw-leic51d-14/tree/main/code/jvm/docs/rels/"

    val SELF = LinkRelation("self")

    val USER = LinkRelation(
        BASE_URL + "user"
    )

    val NEXT = LinkRelation("next")

    val PREV = LinkRelation("prev")

    val FIRST = LinkRelation("first")

    val LAST = LinkRelation("last")
}
