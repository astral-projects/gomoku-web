package gomoku.http.pipeline.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

/**
 * Filter that logs each request.
 */
@Component
class LoggingFilter : HttpFilter() {

    override fun doFilter(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val stopWatch = StopWatch().apply { start() }
        logger.info(
            "method:{}, path:{}, status:{}, duration:{} ms",
            request.method,
            request.requestURI,
            response.status,
            0
        )
        chain.doFilter(request, response)
        stopWatch.stop()
        logger.info(
            "method:{}, path:{}, status:{}, duration:{} ms",
            request.method,
            request.requestURI,
            response.status,
            stopWatch.totalTimeMillis
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)
    }
}
