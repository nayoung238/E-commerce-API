package com.nayoung.orderservice.web;

import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
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

    @PostMapping("/{accountId}")
    public ResponseEntity<?> create(@PathVariable Long accountId, @RequestBody OrderRequest orderRequest) {
        orderRequest.setCustomerAccountId(accountId);
        OrderResponse response = orderService.create(orderRequest);

        orderRequest.setOrderId(response.getOrderId());
        kafkaProducer.send("update-stock-topic", orderRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{customerAccountId}/{lastOrderId}", "/{customerAccountId}"})
    public ResponseEntity<?> getOrders(@PathVariable Long customerAccountId, @PathVariable(required = false) Long lastOrderId) {
        List<OrderResponse> response = orderService.findOrderByCustomerAccountIdAndOrderId(customerAccountId, lastOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
