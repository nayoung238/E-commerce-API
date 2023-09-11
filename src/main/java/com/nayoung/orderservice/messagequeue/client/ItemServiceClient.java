package com.nayoung.orderservice.messagequeue.client;

import com.nayoung.orderservice.web.dto.ItemUpdateLogDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("item-service")
public interface ItemServiceClient {

    @GetMapping("/itemUpdateLogs/{orderId}")
    List<ItemUpdateLogDto> getItemUpdateLogDtos(@PathVariable Long orderId);
}
