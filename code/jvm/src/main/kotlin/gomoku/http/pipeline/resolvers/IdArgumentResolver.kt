package gomoku.http.pipeline.argumentResolvers

import gomoku.domain.Id
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Resolves the [Id] argument from the request path.
 */
@Component
class IdArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.parameterType == Id::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        logger.info("Resolving Id argument")
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("No HttpServletRequest found")

        val id = request.requestURI.split("/")[3].toIntOrNull()
            ?: throw IllegalStateException("No id parameter found")
        return try {
            Id(id)
        } catch (e: IllegalArgumentException) {
            logger.error("Failed to resolve Id argument", e)
            e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IdArgumentResolver::class.java)
    }
}
