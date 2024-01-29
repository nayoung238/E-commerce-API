package com.nayoung.orderservice.openfeign;

import com.nayoung.orderservice.domain.OrderItemStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "item-service", url = "http://127.0.0.1:8089/item-service")
public interface ItemServiceClient {

    @GetMapping(value = "/order-processing-result/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrderItemStatus findOrderProcessingResultByEventId(@PathVariable String eventId);
}
