package com.ecommerce.accountservice.openfeign;

import com.ecommerce.accountservice.exception.CustomFeignException;
import com.ecommerce.accountservice.exception.ExceptionCode;
import com.ecommerce.accountservice.openfeign.client.OrderDto;
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

import java.util.List;

import static com.ecommerce.accountservice.resilience4j.Resilience4jCircuitBreakerConfig.ORDER_LIST_CIRCUIT_BREAKER;
import static com.ecommerce.accountservice.resilience4j.Resilience4jRetryConfig.ORDER_LIST_RETRY;

@FeignClient(name = "order-service",
        url = "http://${spring.cloud.discovery.client.simple.local.host}"
                + ":${spring.cloud.discovery.client.simple.local.port}"
                + "/${spring.cloud.discovery.client.simple.local.service-id}")
public interface OrderServiceClient {

    Logger log = LoggerFactory.getLogger(OrderServiceClient.class);

    @Retry(name = ORDER_LIST_RETRY)
    @CircuitBreaker(name = ORDER_LIST_CIRCUIT_BREAKER, fallbackMethod = "fallback")
    @GetMapping(value = "/orders/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrderDto> getOrders(@PathVariable Long userId);

    @Retry(name = ORDER_LIST_RETRY)
    @CircuitBreaker(name = ORDER_LIST_CIRCUIT_BREAKER, fallbackMethod = "fallback")
    @GetMapping(value = "/orders/{userId}/{cursorOrderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrderDto> getOrdersByCursorOrderId(@PathVariable Long userId, @PathVariable Long cursorOrderId);

    default List<OrderDto> fallback(RetryableException e) {
        log.error("RetryableException: " + e.getMessage());
        throw new CustomFeignException(ExceptionCode.TEMPORARILY_UNAVAILABLE);
    }

    default List<OrderDto> fallback(FeignException.FeignClientException e) {
        log.error("FeignClientException: " + e.getMessage());
        throw new CustomFeignException(ExceptionCode.TEMPORARILY_UNAVAILABLE);
    }

    default List<OrderDto> fallback(FeignException.FeignServerException e) {
        log.error("FeignServerException: " + e.getMessage());
        throw new CustomFeignException(ExceptionCode.TEMPORARILY_UNAVAILABLE);
    }

    default List<OrderDto> fallback(CallNotPermittedException e) {
        log.error("CallNotPermittedException: " + e.getMessage());
        throw new CustomFeignException(ExceptionCode.TEMPORARILY_UNAVAILABLE);
    }
}
