package com.example.apigatewayresearch.controller

import io.github.resilience4j.ratelimiter.RequestNotPermitted
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ErrorController {

    @ExceptionHandler(RequestNotPermitted::class)
    fun requestNotPermitted(requestNotPermitted: RequestNotPermitted): String {
        return "Ups"
    }

}