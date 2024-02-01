package com.nayoung.orderservice.resilience4j;

import feign.FeignException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jRetryConfig {

   public static final String ORDER_PROCESSING_RESULT_RETRY = "orderProcessingResultRetry";

    @Bean(name = ORDER_PROCESSING_RESULT_RETRY)
    public RetryRegistry retryConfig() {
        return RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(Duration.ofMillis(5000), 2))  // waitDuration 같이 쓰면 오류
                .retryExceptions(FeignException.FeignClientException.class)
                .retryOnException(throwable -> !(throwable instanceof FeignException.FeignServerException))
                .build());

        // https://resilience4j.readme.io/docs/retry#create-and-configure-retry
    }
}
