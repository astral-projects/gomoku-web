package gomoku.http.pipeline.interceptors

import com.fasterxml.jackson.databind.ObjectMapper
import gomoku.domain.user.AuthenticatedUser
import gomoku.http.media.Problem
import gomoku.http.pipeline.interceptors.AuthenticationInterceptor.Companion.NAME_WWW_AUTHENTICATE_HEADER
import gomoku.http.pipeline.processors.RequestTokenProcessor
import gomoku.http.pipeline.resolvers.AuthenticatedUserArgumentResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.net.URI

/**
 * Interceptor that checks if the handler method requires authentication and if so, it
 * requests to [RequestTokenProcessor] to process the token and build an [AuthenticatedUser] with it.
 * If the token is invalid, it short-circuits the request and returns a 401 with
 * the [RequestTokenProcessor.SCHEME] in the [NAME_WWW_AUTHENTICATE_HEADER] header.
 */
@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        logger.info("Authentication interceptor")
        if (handler is HandlerMethod && handler.methodParameters.any { it.parameterType == AuthenticatedUser::class.java }) {
            logger.info("Handler method requires authentication")
            // process token in authentication schema
            val bearerToken = request.getHeader(NAME_AUTHORIZATION_HEADER)
            val authUser = authorizationHeaderProcessor
                .processAuthorizationHeaderValue(bearerToken)
            logger.info("Bearer Token: $bearerToken")
            // process token in cookie
            val authCookie = request.cookies?.find { it.name == NAME_COOKIE }?.value
            val authUserCookie = authorizationHeaderProcessor
                .processCookieValue(authCookie)
            logger.info("Cookie: $authCookie")
            return if (authUser == null && authUserCookie == null) {
                // short-circuit this request since the client is not authenticated
                response.contentType = Problem.MEDIA_TYPE
                // add problem media to response
                val objectMapper = ObjectMapper()
                val problem = Problem(
                    type = Problem.unauthorizedRequest,
                    title = "Unauthorized Request",
                    status = 401,
                    detail = "User not authenticated",
                    instance = URI(request.requestURI)
                )
                val json = objectMapper.writeValueAsString(problem)
                response.writer.write(json)
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                logger.info("User not authenticated")
                false
            } else {
                (authUser ?: authUserCookie)?.let { AuthenticatedUserArgumentResolver.addUserTo(it, request) }
                logger.info("User authenticated")
                true
            }
        }
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_COOKIE = "_autho"
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
