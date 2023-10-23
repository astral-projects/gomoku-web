package gomoku.http.pipeline.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class RequestIdFilter : HttpFilter() {

    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val generatedId = UUID.randomUUID().toString()
        logger.info("Request ID: $generatedId")
        response.addHeader(KEY, generatedId)
        chain.doFilter(request, response)
    }

    companion object {
        const val KEY = "Request-Id"
        private val logger = LoggerFactory.getLogger(RequestIdFilter::class.java)
    }
}