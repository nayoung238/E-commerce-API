package com.ecommerce.orderservice.domain.order.api;

import com.ecommerce.orderservice.domain.order.dto.OrderListDto;
import com.ecommerce.orderservice.domain.order.service.OrderCreationService;
import com.ecommerce.orderservice.domain.order.service.OrderService;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderCreationService orderCreationService;
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid OrderDto orderDto) {
        OrderDto response = orderCreationService.create(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{accountId}/{cursorOrderId}", "/{accountId}"})
    public ResponseEntity<?> getOrderList(@PathVariable @Valid @Positive(message = "사용자 아이디는 1 이상이어야 합니다.") Long accountId,
                                          @PathVariable(required = false) @Valid @Positive(message = "주문 커서 아이디는 1 이상이어야 합니다.") Long cursorOrderId) {
        OrderListDto response = orderService.findOrderByAccountIdAndOrderId(accountId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        log.info("health check...");
        return ResponseEntity.ok("health check...");
    }
}
