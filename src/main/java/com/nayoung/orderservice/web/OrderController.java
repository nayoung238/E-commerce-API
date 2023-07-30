package com.nayoung.orderservice.web;

import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody OrderDto orderDto) {
        OrderDto response = orderService.create(orderDto);
        kafkaProducer.send("update-stock-topic", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{customerAccountId}/{lastOrderId}", "/{customerAccountId}"})
    public ResponseEntity<?> getOrders(@PathVariable Long customerAccountId, @PathVariable(required = false) Long lastOrderId) {
        List<OrderDto> response = orderService.findOrderByCustomerAccountIdAndOrderId(customerAccountId, lastOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
