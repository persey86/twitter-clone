package com.example.twitterclone.config

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException)
    ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body([
                timestamp : Instant.now(),
                error     : "Bad Request",
                message   : ex.message,
                status    : 400
        ] as Map<String, Object>)
    }

    @ExceptionHandler(NoSuchElementException)
    ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body([
                timestamp : Instant.now(),
                error     : "Not Found Requested Source",
                message   : ex.message,
                status    : 404
        ] as Map<String, Object>)
    }
}
