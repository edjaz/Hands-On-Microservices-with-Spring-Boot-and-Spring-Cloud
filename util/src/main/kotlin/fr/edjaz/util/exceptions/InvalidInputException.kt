package fr.edjaz.util.exceptions

import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import java.net.InetAddress
import java.time.ZonedDateTime
import org.springframework.web.bind.annotation.RestControllerAdvice
import fr.edjaz.util.http.HttpErrorInfo
import fr.edjaz.util.exceptions.InvalidInputException
import fr.edjaz.util.http.GlobalControllerExceptionHandler
import java.lang.RuntimeException

class InvalidInputException : RuntimeException {
    constructor() {}
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
}
