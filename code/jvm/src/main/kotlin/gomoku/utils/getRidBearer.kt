package gomoku.utils

fun getRidBearer(token: String): String  = token.substring("Bearer ".length).trim()


