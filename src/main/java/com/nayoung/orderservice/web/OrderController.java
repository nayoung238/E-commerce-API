package com.nayoung.orderservice.web;

import com.nayoung.orderservice.domain.service.OrderCreationService;
import com.nayoung.orderservice.domain.service.OrderService;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderCreationService orderCreationService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createByKStream(@RequestBody @Validated OrderDto orderDto) {
        OrderDto response = orderCreationService.create(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{userId}/{cursorOrderId}", "/{userId}"})
    public ResponseEntity<?> getOrders(@PathVariable Long userId, @PathVariable(required = false) Long cursorOrderId) {
        List<OrderDto> response = orderService.findOrderByUserIdAndOrderId(userId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
