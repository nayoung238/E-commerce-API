package com.ecommerce.accountservice.resilience4j;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jRetryConfig {

    public static final String ORDER_LIST_RETRY = "orderListRetry";

    @Bean(name = ORDER_LIST_RETRY)
    public RetryRegistry retryRegistry() {
        return RetryRegistry.of(
                RetryConfig.custom()
                        .maxAttempts(3)
                        .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(3000), 2))
                        .retryExceptions(FeignException.FeignServerException.class)
                        .retryOnException(
                                throwable -> !(throwable instanceof FeignException.FeignClientException)
                                        && !(throwable instanceof RetryableException))
                        .build());
    }
}
