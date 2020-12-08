package fr.edjaz.util.http

import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.concurrent.TimeoutException

@RestControllerAdvice
internal class GlobalControllerExceptionHandler {
    private val LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler::class.java)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        NotFoundException::class
    )
    @ResponseBody
    fun handleNotFoundExceptions(request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex)
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(
        InvalidInputException::class
    )
    @ResponseBody
    fun handleInvalidInputException(request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        TypeMismatchException::class
    )
    @ResponseBody
    fun handleTypeMismatchException(request: ServerHttpRequest, ex: Exception?): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.BAD_REQUEST, request, "Type mismatch.")
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(
        TimeoutException::class
    )
    @ResponseBody
    fun handleTypeTimeoutException(request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request, ex.message)
    }

    private fun createHttpErrorInfo(
        httpStatus: HttpStatus,
        request: ServerHttpRequest,
        message: String?
    ): HttpErrorInfo {
        val path = request.path.pathWithinApplication().value()
        LOG.debug(
            "Returning HTTP status: {} for path: {}, message: {}",
            httpStatus,
            path,
            message
        )
        return HttpErrorInfo(httpStatus = httpStatus, path= path, message = message)
    }

    private fun createHttpErrorInfo(httpStatus: HttpStatus, request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        val path = request.path.pathWithinApplication().value()
        val message = ex.message
        LOG.debug(
            "Returning HTTP status: {} for path: {}, message: {}",
            httpStatus,
            path,
            message
        )
      return HttpErrorInfo(httpStatus = httpStatus, path= path, message = message)
    }
}
