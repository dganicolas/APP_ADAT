package com.es.aplicacion.error.exception

class BadRequestException(message: String) : Exception("Bad request exception (400). $message") {
}