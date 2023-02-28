package com.nayoung.orderservice.web;

import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class OrderController {

    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;

    @PostMapping("{accountId}/orders")
    public ResponseEntity<?> create(@PathVariable Long accountId, @RequestBody OrderRequest orderRequest) {
        orderRequest.setAccountId(accountId);
        OrderResponse response = orderService.create(orderRequest);

        kafkaProducer.send("update-stock-topic", orderRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("{accountId}/orders")
    public ResponseEntity<?> getOrders(@PathVariable Long accountId) {
        Iterable<OrderResponse> response = orderService.getOrdersByAccountId(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
