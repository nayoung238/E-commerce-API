package com.nayoung.orderservice.resilience4j;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4JConfig {

    public static final String ORDER_ITEM_UPDATE_RESULT = "orderItemUpdateResult";
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerConfig() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofMillis(10000))  // open -> half open
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(3)
                .minimumNumberOfCalls(2)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(30000))
                .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()
        );
    }

    @Bean
    public Retry orderItemUpdateResultRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(2000), 2))  // waitDuration 같이 쓰면 오류
                .retryExceptions(FeignException.class)
                .build();

        return Retry.of(ORDER_ITEM_UPDATE_RESULT, config);
    }
}