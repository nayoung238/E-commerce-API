package com.nayoung.orderservice.openfeign;

import com.nayoung.orderservice.openfeign.dto.ItemUpdateLogDto;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "item-service",
        url = "http://127.0.0.1:8080/item-service/%s")
public interface ItemServiceClient {

    @Retry(name = "order-result")
    @GetMapping(value = "/item-update-log/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ItemUpdateLogDto> findAllOrderItemUpdateResultByEventId(@PathVariable String eventId);
}
