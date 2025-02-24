package com.ecommerce.apigatewayservice.fallback;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
public class FallbackController {

    @GetMapping("/fallback")
    public void serviceFallback(ServerWebExchange exchange) {
        Throwable t = exchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        // TODO: 수정 예정
        if(t instanceof NotFoundException) {
            log.error(t.toString());
        } else if(t instanceof TimeoutException) {
            log.error(t.toString());
        } else if(t instanceof CallNotPermittedException) {
            log.error(t.toString());
        } else {
            log.error(Objects.requireNonNull(t).toString());
        }
    }
}
