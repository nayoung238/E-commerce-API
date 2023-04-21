package com.nayoung.accountservice.client;

import com.nayoung.accountservice.web.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @GetMapping("/orders/{customerAccountId}/{lastOrderId}")
    List<OrderResponse> getOrders(@PathVariable Long customerAccountId, @PathVariable Long lastOrderId);
}
