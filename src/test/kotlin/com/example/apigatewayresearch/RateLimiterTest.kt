package com.example.apigatewayresearch

import io.github.resilience4j.kotlin.ratelimiter.RateLimiterRegistry
import org.junit.jupiter.api.Test


class RateLimiterTest {

    @Test
    internal fun testRateLimiterSlow() {
        val registry = RateLimiterRegistry {  }
        val rateLimiter = registry.rateLimiter("test")
        println(rateLimiter.rateLimiterConfig.limitForPeriod)
        rateLimiter.changeLimitForPeriod(rateLimiter.rateLimiterConfig.limitForPeriod * 2)
        println(rateLimiter.rateLimiterConfig.limitForPeriod)
    }
}