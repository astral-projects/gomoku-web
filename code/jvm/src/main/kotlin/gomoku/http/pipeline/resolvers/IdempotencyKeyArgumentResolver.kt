package gomoku.http.pipeline.resolvers

import gomoku.domain.idempotencyKey.IdempotencyKey
import gomoku.http.pipeline.errors.HttpServletRequestRequiredException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Retrieves the [IdempotencyKey] from the request attributes.
 */
@Component
class IdempotencyKeyArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.parameterType == IdempotencyKey::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw HttpServletRequestRequiredException()
        return getIdempotencyKeyFrom(request)
    }

    companion object {
        private const val KEY = "IdempotencyKeyArgumentResolver"

        fun addIdempotencyKeyTo(idempotencyKey: IdempotencyKey, request: HttpServletRequest) =
            request.setAttribute(KEY, idempotencyKey)

        fun getIdempotencyKeyFrom(request: HttpServletRequest): IdempotencyKey? =
            request.getAttribute(KEY)?.let { it as? IdempotencyKey }
    }
}
