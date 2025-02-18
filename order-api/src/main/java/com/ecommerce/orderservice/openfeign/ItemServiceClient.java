package com.ecommerce.orderservice.openfeign;

import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
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

import static com.ecommerce.orderservice.resilience4j.Resilience4jCircuitBreakerConfig.ORDER_PROCESSING_RESULT_CIRCUIT_BREAKER;
import static com.ecommerce.orderservice.resilience4j.Resilience4jRetryConfig.ORDER_PROCESSING_RESULT_RETRY;

@FeignClient(name = "item-service",
        url = "http://${spring.cloud.discovery.client.simple.local.host}"
                + ":${spring.cloud.discovery.client.simple.local.port}"
                + "/${spring.cloud.discovery.client.simple.local.service-id}")
public interface ItemServiceClient {

    Logger log = LoggerFactory.getLogger(ItemServiceClient.class);

    // Retry 우선순위를 CircuitBreaker 보다 높게 설정
    @Retry(name = ORDER_PROCESSING_RESULT_RETRY)
    @CircuitBreaker(name = ORDER_PROCESSING_RESULT_CIRCUIT_BREAKER, fallbackMethod = "fallback")
    @GetMapping(value = "/order-processing-result/{orderEventKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrderProcessingStatus findOrderProcessingResult(@PathVariable String orderEventKey);

    default OrderProcessingStatus fallback(RetryableException e) {
        log.error("RetryableException: " + e.getMessage());
        return OrderProcessingStatus.SERVER_ERROR;
    }

    default OrderProcessingStatus fallback(FeignException.FeignClientException e) {
        log.error("FeignClientException: " + e.getMessage());
        return OrderProcessingStatus.NOT_EXIST;
    }

    default OrderProcessingStatus fallback(FeignException.FeignServerException e) {
        log.error("FeignServerException: " + e.getMessage());
        return OrderProcessingStatus.SERVER_ERROR;
    }

    default OrderProcessingStatus fallback(CallNotPermittedException e) {
        log.error("CallNotPermittedException: " + e.getMessage());
        return OrderProcessingStatus.FAILED;
    }
}
