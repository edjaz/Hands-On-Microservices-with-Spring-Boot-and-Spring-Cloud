package fr.edjaz.util.http

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

class HttpErrorInfo {
    var timestamp: ZonedDateTime?
    var path: String?
    var httpStatus: HttpStatus?
    var message: String?

    constructor() {
        timestamp = ZonedDateTime.now()
        this.httpStatus = null
        this.path = null
        this.message = null
    }

    constructor(httpStatus: HttpStatus?, path: String?, message: String?) {
        timestamp = ZonedDateTime.now()
        this.httpStatus = httpStatus
        this.path = path
        this.message = message
    }

    fun getError(): String = httpStatus!!.reasonPhrase
}
