package gomoku.utils

import org.springframework.http.ResponseEntity

fun getRidBearer(token: String): String  = token.substring("Bearer ".length).trim()


