package com.example.apigatewayresearch.filter

import com.example.apigatewayresearch.model.RateLimiterConfig
import com.example.apigatewayresearch.service.RateLimiterService
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TimeGatewayFilterFactory(
    val rateLimiterService: RateLimiterService
) : AbstractGatewayFilterFactory<RateLimiterConfig>(RateLimiterConfig::class.java) {

    override fun apply(config: RateLimiterConfig): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val startTime = System.currentTimeMillis()
            chain.filter(exchange)
                .then(Mono.defer {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    rateLimiterService.increase(config.rateLimiter, duration)
                })
        }
    }

}