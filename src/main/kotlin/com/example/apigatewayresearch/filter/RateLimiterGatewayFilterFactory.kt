package com.example.apigatewayresearch.filter

import com.example.apigatewayresearch.model.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component

@Component
class RateLimiterGatewayFilterFactory(
    val rateLimiterRegistry: RateLimiterRegistry
) : AbstractGatewayFilterFactory<RateLimiterConfig>(RateLimiterConfig::class.java) {

    override fun apply(config: RateLimiterConfig): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            chain.filter(exchange)
                .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter(config.rateLimiter)))
        }
    }
}