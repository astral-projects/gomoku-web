package gomoku.http.pipeline.interceptors

import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.http.pipeline.IdempotencyKeyProcessor
import gomoku.http.pipeline.resolvers.IdempotencyKeyArgumentResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Interceptor that checks if the handler method requires an idempotency key.
 * If so, it processes the idempotency key in the request and adds the [IdempotencyKey] to it.
 */
@Component
class IdempotencyKeyInterceptor(
    private val idempotencyKeyProcessor: IdempotencyKeyProcessor
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        logger.info("Checking if request requires idempotency key")
        if (handler is HandlerMethod && handler.methodParameters.any { it.parameterType == IdempotencyKey::class.java }) {
            logger.info("Request requires idempotency key")
            val key = request.getHeader(NAME_IDEMPOTENCY_KEY_HEADER)
            val idempotencyKey = idempotencyKeyProcessor.processIdempotencyKey(key)
            if (idempotencyKey == null) {
                logger.info("Idempotency key not found")
                response.status = 400
                response.addHeader(NAME_IDEMPOTENCY_KEY_HEADER, "Idempotency key not found")
                return false
            } else {
                IdempotencyKeyArgumentResolver.addIdempotencyKeyTo(idempotencyKey, request)
                logger.info("Idempotency key found")
            }
        }
        return true
    }
    companion object {
        val logger = LoggerFactory.getLogger(IdempotencyKeyInterceptor::class.java)
        const val NAME_IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
    }
}
