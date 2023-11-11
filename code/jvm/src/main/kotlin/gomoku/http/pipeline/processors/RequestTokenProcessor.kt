package gomoku.http.pipeline.processors

import gomoku.domain.user.AuthenticatedUser
import gomoku.services.user.UsersService
import org.springframework.stereotype.Component

@Component
class RequestTokenProcessor(
    val usersService: UsersService
) {

    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        // Bearer <token>
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        val schemeString = parts.first()
        val tokenString = parts.last()
        if (schemeString.lowercase() != SCHEME) {
            return null
        }
        return usersService.getUserByToken(tokenString)?.let {
            AuthenticatedUser(
                it,
                tokenString
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
