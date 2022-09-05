package com.example.apigatewayresearch.scheduler

import com.example.apigatewayresearch.exception.NoRequestException
import com.example.apigatewayresearch.properties.RateLimiterMetricProperties
import com.example.apigatewayresearch.service.RateLimiterService
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


@Component
class RateLimiterScheduler(
    val rateLimiterRegistry: RateLimiterRegistry,
    val rateLimiterService: RateLimiterService,
    val rateLimiterMetricProperties: RateLimiterMetricProperties
) {

    @Scheduled(cron = "0 * * * * *")
    fun increaseRateLimiter() {
        rateLimiterMetricProperties.metrics.forEach { (name, metric) ->
            Mono.zip(
                rateLimiterService.isSlow(name),
                rateLimiterService.getMaxLimit(name)
            )
                .flatMap { changeMaxLimit(it.t1, it.t2, metric, name) }
                .doOnSuccess { changeRateLimiterConfig(name, it) }
                .doOnError(NoRequestException::class.java) { println("No request, do nothing") }
                .then(rateLimiterService.reset(name))
                .subscribe(
                    { println("Success run scheduler") },
                    { println("Ups, exception happens ${it.message}") }
                )
        }
    }

    private fun changeRateLimiterConfig(name: String, newLimit: Int): Unit {
        val rateLimit = rateLimiterRegistry.rateLimiter(name)
        if (newLimit != rateLimit.rateLimiterConfig.limitForPeriod) {
            println("change the limit to $newLimit")
            rateLimit.changeLimitForPeriod(newLimit)
        }
    }

    private fun changeMaxLimit(
        isSlow: Boolean,
        maxLimit: Int,
        metric: RateLimiterMetricProperties.RateLimiterMetricDetailProperties,
        name: String
    ) = if (isSlow) {
        println("is slow")
        decreaseLimit(maxLimit, metric, name)
    } else {
        println("is not slow")
        increaseLimit(maxLimit, metric, name)
    }

    private fun increaseLimit(
        maxLimit: Int,
        metric: RateLimiterMetricProperties.RateLimiterMetricDetailProperties,
        name: String
    ): Mono<Int> {
        return if (maxLimit >= metric.maxLimitForPeriod) {
            rateLimiterService.changeMaxLimit(name, metric.maxLimitForPeriod)
        } else {
            rateLimiterService.changeMaxLimit(name, maxLimit * 2)
        }
    }

    private fun decreaseLimit(
        maxLimit: Int,
        metric: RateLimiterMetricProperties.RateLimiterMetricDetailProperties,
        name: String
    ): Mono<Int> {
        val newMaxLimit = maxLimit / 2
        return if (newMaxLimit < metric.minLimitForPeriod) {
            rateLimiterService.changeMaxLimit(name, metric.minLimitForPeriod)
        } else {
            rateLimiterService.changeMaxLimit(name, newMaxLimit)
        }
    }
}