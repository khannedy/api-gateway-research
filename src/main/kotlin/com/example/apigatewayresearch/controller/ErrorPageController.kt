package com.example.apigatewayresearch.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
class ErrorPageController {

    @GetMapping("/error")
    fun errorPage(serverWebExchange: ServerWebExchange): String {
        return "Ups"
    }
}