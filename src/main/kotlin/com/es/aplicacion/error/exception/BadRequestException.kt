package com.es.aplicacion.error.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity

class BadRequestException(message: String) : Exception("Bad request exception (400). $message") {
    init {
        ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }
}