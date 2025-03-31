package com.ecommerce.orderservice.common.config;

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

   public static final String ORDER_PROCESSED_RESULT_RETRY = "orderProcessedResultRetry";

    @Bean(name = ORDER_PROCESSED_RESULT_RETRY)
    public RetryRegistry retryConfig() {
        return RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(5000), 2))  // waitDuration 같이 쓰면 오류
                .retryExceptions(FeignException.FeignClientException.class)
                .retryOnException(
                        throwable -> !(throwable instanceof FeignException.FeignServerException)
                                && !(throwable instanceof RetryableException))
                .build());

        // https://resilience4j.readme.io/docs/retry#create-and-configure-retry
    }
}
