package com.nayoung.orderservice.web;

import com.nayoung.orderservice.domain.OrderService;
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

    private final OrderService orderService;

    @PostMapping("/v1")
    public ResponseEntity<?> create(@RequestBody @Validated OrderDto orderDto) {
        OrderDto response = orderService.create(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/v2")
    public ResponseEntity<?> createByKStream(@RequestBody @Validated OrderDto orderDto) {
        OrderDto response = orderService.createByKStream(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{customerAccountId}/{cursorOrderId}", "/{customerAccountId}"})
    public ResponseEntity<?> getOrders(@PathVariable Long customerAccountId, @PathVariable(required = false) Long cursorOrderId) {
        List<OrderDto> response = orderService.findOrderByCustomerAccountIdAndOrderId(customerAccountId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
