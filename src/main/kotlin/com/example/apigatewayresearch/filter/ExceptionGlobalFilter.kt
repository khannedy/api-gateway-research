package com.example.apigatewayresearch.filter

import io.github.resilience4j.ratelimiter.RequestNotPermitted
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class ExceptionGlobalFilter : GlobalFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .onErrorResume(RequestNotPermitted::class.java) {
                exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                exchange.response.headers.add("X-Rate-Limit-Error", "true")
                exchange.response.headers.add("Content-Type", "text/html")

                val errorResponse: ByteArray = "Too Many Request".toByteArray(StandardCharsets.UTF_8)
                val buffer: DataBuffer = exchange.response.bufferFactory().wrap(errorResponse)

                exchange.response.writeWith(Mono.just(buffer))
            }
    }

    override fun getOrder(): Int {
        return -1
    }
}