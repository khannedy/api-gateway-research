package com.example.apigatewayresearch.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("rate-limiter")
data class RateLimiterMetricProperties(
    var metrics: Map<String, RateLimiterMetricDetailProperties> = mutableMapOf()
) {

    data class RateLimiterMetricDetailProperties(
        var maxLimit: Int = 1000,
        var minLimit: Int = 10,
        var slowDuration: Duration = Duration.ofMillis(500),
    )

}