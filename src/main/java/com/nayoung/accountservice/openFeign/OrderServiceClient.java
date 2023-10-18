package com.nayoung.accountservice.openFeign;

import com.nayoung.accountservice.openFeign.client.OrderDto;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @Retry(name = Resilience4JConfig.ORDER_LIST_RETRY_NAME)
    @GetMapping("/orders/{customerAccountId}/{cursorOrderId}")
    List<OrderDto> getOrders(@PathVariable Long customerAccountId, @PathVariable Long cursorOrderId);
}
