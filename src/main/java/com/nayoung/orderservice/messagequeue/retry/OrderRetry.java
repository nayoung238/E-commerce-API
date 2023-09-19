package com.nayoung.orderservice.messagequeue.retry;

import feign.FeignException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class OrderRetry {

    //@Bean
    public Retry stockUpdateResult() {
        IntervalFunction intervalFunction = IntervalFunction.ofExponentialRandomBackoff(5000L, 2);
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(intervalFunction)
                .retryExceptions(FeignException.class)
                .build();

        return Retry.of("stockUpdateResultRetry", retryConfig);
    }
}