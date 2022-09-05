package com.example.apigatewayresearch.filter

import com.example.apigatewayresearch.model.RateLimiterConfig
import com.example.apigatewayresearch.service.RateLimiterService
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

@Component
class TimeGatewayFilterFactory(
    val rateLimiterService: RateLimiterService,
    val meterRegistry: MeterRegistry
) : AbstractGatewayFilterFactory<RateLimiterConfig>(RateLimiterConfig::class.java) {

    override fun apply(config: RateLimiterConfig): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val startTime = System.currentTimeMillis()
            chain.filter(exchange)
                .then(Mono.defer {
                    meterRegistry.counter("api_gateway_request_success_count", "rateLimiter", config.rateLimiter)
                        .increment()

                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    meterRegistry.timer("api_gateway_request_duration", "rateLimiter", config.rateLimiter)
                        .record(duration, TimeUnit.MILLISECONDS)

                    rateLimiterService.increase(config.rateLimiter, duration)
                })
        }
    }

}