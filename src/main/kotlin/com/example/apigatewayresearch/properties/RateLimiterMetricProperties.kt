package com.example.apigatewayresearch.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("rate-limiter")
data class RateLimiterMetricProperties(
    var metrics: Map<String, RateLimiterMetricDetailProperties> = mutableMapOf()
) {

    data class RateLimiterMetricDetailProperties(
        var maxLimitForPeriod: Int = 10,
        var minLimitForPeriod: Int = 10,
        var limitRefreshPeriod: Duration = Duration.ofMinutes(1),
        var slowDuration: Duration = Duration.ofMillis(500),
    )

}