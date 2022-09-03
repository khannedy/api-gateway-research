package com.example.apigatewayresearch

import com.example.apigatewayresearch.properties.RateLimiterMetricProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(
    RateLimiterMetricProperties::class
)
class ApiGatewayResearchApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayResearchApplication>(*args)
}
