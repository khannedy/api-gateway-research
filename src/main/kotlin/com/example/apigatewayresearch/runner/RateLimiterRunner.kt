package com.example.apigatewayresearch.runner

import com.example.apigatewayresearch.properties.RateLimiterMetricProperties
import com.example.apigatewayresearch.service.RateLimiterService
import io.github.resilience4j.kotlin.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class RateLimiterRunner(
    val rateLimiterMetricProperties: RateLimiterMetricProperties,
    val rateLimiterRegistry: RateLimiterRegistry,
    val meterRegistry: MeterRegistry,
    val rateLimiterService: RateLimiterService
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        registerRateLimiters()
        registerRateLimiterMetrics()
    }

    private fun registerRateLimiters() {
        rateLimiterMetricProperties.metrics.forEach { (name, metric) ->
            RateLimiterConfig {
                limitForPeriod(getMaxLimitForPeriod(name, metric.maxLimitForPeriod))
                limitRefreshPeriod(metric.limitRefreshPeriod)
            }.let { config ->
                rateLimiterRegistry.rateLimiter(name, config)
            }
        }
    }

    private fun getMaxLimitForPeriod(name: String, default: Int): Int {
        return rateLimiterService.getMaxLimit(name)
            .defaultIfEmpty(default)
            .block()!!
    }

    private fun registerRateLimiterMetrics() {
        rateLimiterMetricProperties.metrics.forEach { (name, metric) ->
            Gauge.builder("resilience4j.ratelimiter.limit_for_period") {
                rateLimiterRegistry.rateLimiter(name).rateLimiterConfig.limitForPeriod
            }.description("The limit for the period")
                .tag("rate_limiter", name)
                .register(meterRegistry)
        }
    }
}