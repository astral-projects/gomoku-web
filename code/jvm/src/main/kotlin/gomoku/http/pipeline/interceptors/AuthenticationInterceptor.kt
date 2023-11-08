package gomoku.http.pipeline.interceptors

import gomoku.domain.user.AuthenticatedUser
import gomoku.http.controllers.RequiresAuthentication
import gomoku.http.pipeline.RequestTokenProcessor
import gomoku.http.pipeline.interceptors.AuthenticationInterceptor.Companion.NAME_WWW_AUTHENTICATE_HEADER
import gomoku.http.pipeline.resolvers.AuthenticatedUserArgumentResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Interceptor that checks if the handler method requires authentication and if so, it
 * requests to [RequestTokenProcessor] to process the token and build an [AuthenticatedUser] with it.
 * If the token is invalid, it short-circuits the request and returns a 401 with
 * the [RequestTokenProcessor.SCHEME] in the [NAME_WWW_AUTHENTICATE_HEADER] header.
 */
@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.hasMethodAnnotation(RequiresAuthentication::class.java)) {
            logger.info("Handler method requires authentication")
            // process token in authentication schema
            val authUser = authorizationHeaderProcessor
                .processAuthorizationHeaderValue(request.getHeader(NAME_AUTHORIZATION_HEADER))
            return if (authUser == null) {
                // short-circuit this request since the client is not authenticated
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                logger.info("User not authenticated")
                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(authUser, request)
                logger.info("User authenticated")
                true
            }
        }

        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
