package com.nayoung.orderservice.openfeign;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "item-service")
public interface ItemServiceClient {

    @Retry(name = Resilience4JConfig.ORDER_ITEM_UPDATE_RESULT)
    @GetMapping("/item-update-log/{eventId}")
    List<ItemUpdateLogDto> findAllOrderItemUpdateResultByEventId(@PathVariable String eventId);
}
