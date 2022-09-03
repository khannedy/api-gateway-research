package com.example.apigatewayresearch.service

import com.example.apigatewayresearch.exception.NoRequestException
import com.example.apigatewayresearch.properties.RateLimiterMetricProperties
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono


@Component
class RateLimiterService(
    val rateLimiterMetricProperties: RateLimiterMetricProperties,
    val reactiveStringRedisTemplate: ReactiveStringRedisTemplate
) : InitializingBean {

    override fun afterPropertiesSet() {
        rateLimiterMetricProperties.metrics.forEach { (key, value) ->
            Mono.zip(
                reactiveStringRedisTemplate.opsForValue().setIfAbsent("${key}_duration", "0"),
                reactiveStringRedisTemplate.opsForValue().setIfAbsent("${key}_request", "0"),
                reactiveStringRedisTemplate.opsForValue().setIfAbsent("${key}_max_limit", value.maxLimit.toString()),
            ).block()
        }
    }

    fun increase(key: String, duration: Long): Mono<Void> {
        return Mono.zip(
            reactiveStringRedisTemplate.opsForValue().increment("${key}_duration", duration),
            reactiveStringRedisTemplate.opsForValue().increment("${key}_request", 1)
        ).then()
    }

    fun reset(key: String): Mono<Void> {
        return Mono.zip(
            reactiveStringRedisTemplate.opsForValue().set("${key}_duration", "0"),
            reactiveStringRedisTemplate.opsForValue().set("${key}_request", "0")
        ).then()
    }

    fun isSlow(key: String): Mono<Boolean> {
        return Mono.zip(
            reactiveStringRedisTemplate.opsForValue().get("${key}_duration"),
            reactiveStringRedisTemplate.opsForValue().get("${key}_request")
        ).map {
            val duration = it.t1.toLong()
            val request = it.t2.toLong()
            val slowDuration = rateLimiterMetricProperties.metrics[key]!!.slowDuration.toMillis()

            if (request == 0L) {
                throw NoRequestException("No request for rate limiter $key")
            } else {
                duration / request > slowDuration
            }
        }
    }

    fun changeMaxLimit(key: String, maxLimit: Int): Mono<Int> {
        return reactiveStringRedisTemplate.opsForValue().set("${key}_max_limit", maxLimit.toString())
            .thenReturn(maxLimit)
    }

    fun getMaxLimit(key: String): Mono<Int> {
        return reactiveStringRedisTemplate.opsForValue().get("${key}_max_limit")
            .map { it.toInt() }
    }

}