package com.nayoung.orderservice.openfeign;

import com.nayoung.orderservice.messagequeue.client.ItemUpdateLogDto;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("item-service")
public interface ItemServiceClient {

    @Retry(name = "orderDetailsResult")
    @GetMapping("/itemUpdateLogs/{orderId}")
    List<ItemUpdateLogDto> getItemUpdateLogDtos(@PathVariable Long orderId);
}
