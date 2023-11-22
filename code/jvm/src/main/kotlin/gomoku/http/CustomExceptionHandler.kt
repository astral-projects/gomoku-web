package gomoku.http

import gomoku.http.media.Problem
import gomoku.http.pipeline.errors.HttpServletRequestRequiredException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
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
        return Problem(
            type = Problem.invalidRequestContent,
            title = "Method argument not valid",
            status = 400,
            detail = ex.message
        ).toResponse()
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.info("Handling TypeMismatchException: {}", ex.message)
        val type = ex.value?.let { it::class.java.simpleName } ?: "null"
        val detail = "The value '${ex.value}' of type '$type' could not be converted to ${ex.requiredType?.name}"
        val uri = URI(request.toString().substringAfter("uri=").substringBefore("}").substringBefore(";"))
        return Problem(
            type = Problem.invalidRequestContent,
            title = "Invalid Argument",
            status = 400,
            detail = detail,
            instance = uri
        ).toResponse()
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        log.info("Handling HttpMessageNotReadableException: {}", ex.message)

        return Problem(
            type = Problem.invalidRequestContent,
            title = "Http message not readable",
            status = 400,
            detail = ex.message.toString()
        ).toResponse()
    }

    @ExceptionHandler(HttpServletRequestRequiredException::class)
    fun handleHttpServletRequestRequired(): ResponseEntity<Any> {
        log.info("Handling HttpServletRequestRequiredException")
        return Problem(
            type = Problem.invalidRequestContent,
            title = "Http servlet request required",
            status = 503,
            // access exception message
            detail = "A HttpServletRequest is required to resolve this request"
        ).toResponse()
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(): ResponseEntity<Unit> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CustomExceptionHandler::class.java)
    }
}
