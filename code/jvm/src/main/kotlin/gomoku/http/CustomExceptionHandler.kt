package gomoku.http

import gomoku.http.model.Problem
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.info("Handling MethodArgumentNotValidException: {}", ex.message)
        return Problem.response(400, Problem.invalidRequestContent)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        log.info("Handling HttpMessageNotReadableException: {}", ex.message)
        return Problem.response(400, Problem.invalidRequestContent)
    }

    @ExceptionHandler(
        IllegalArgumentException::class
    )
    fun handleArgumentResolutionException(): ResponseEntity<Problem> {
        val problem = Problem(
            URI("")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem)
    }

    @ExceptionHandler(
        Exception::class
    )
    fun handleAll(): ResponseEntity<Unit> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CustomExceptionHandler::class.java)
    }
}
