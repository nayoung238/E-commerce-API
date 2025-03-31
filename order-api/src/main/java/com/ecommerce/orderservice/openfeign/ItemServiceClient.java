package com.ecommerce.orderservice.openfeign;

import com.ecommerce.orderservice.order.enums.OrderStatus;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static com.ecommerce.orderservice.common.config.Resilience4jCircuitBreakerConfig.ORDER_PROCESSED_RESULT_CIRCUIT_BREAKER;
import static com.ecommerce.orderservice.common.config.Resilience4jRetryConfig.ORDER_PROCESSED_RESULT_RETRY;

@FeignClient(name = "item-service")
public interface ItemServiceClient {

    Logger log = LoggerFactory.getLogger(ItemServiceClient.class);

    // Retry 우선순위를 CircuitBreaker 보다 높게 설정
    @Retry(name = ORDER_PROCESSED_RESULT_RETRY)
    @CircuitBreaker(name = ORDER_PROCESSED_RESULT_CIRCUIT_BREAKER, fallbackMethod = "fallback")
    @GetMapping(value = "/order-processed-result/{orderEventKey}", produces = MediaType.APPLICATION_JSON_VALUE)
	OrderStatus findOrderProcessedResult(@PathVariable String orderEventKey);

    default OrderStatus fallback(RetryableException e) {
        log.error("RetryableException: " + e.getMessage());
        return OrderStatus.SERVER_ERROR;
    }

    default OrderStatus fallback(FeignException.FeignClientException e) {
        log.error("FeignClientException: " + e.getMessage());
        return OrderStatus.NOT_EXIST;
    }

    default OrderStatus fallback(FeignException.FeignServerException e) {
        log.error("FeignServerException: " + e.getMessage());
        return OrderStatus.SERVER_ERROR;
    }

    default OrderStatus fallback(CallNotPermittedException e) {
        log.error("CallNotPermittedException: " + e.getMessage());
        return OrderStatus.FAILED;
    }
}
