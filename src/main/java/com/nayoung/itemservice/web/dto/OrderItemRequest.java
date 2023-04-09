package com.nayoung.itemservice.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter @Slf4j
public class OrderItemRequest {

    private Long shopId;
    private Long itemId;
    private Long quantity;

    @Builder
    private OrderItemRequest(Long shopId, Long itemId, Long quantity) {
        this.shopId = shopId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public static OrderItemRequest fromKafkaMessage (Object request) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> map = objectMapper.convertValue(request, Map.class);

        return OrderItemRequest.builder()
                .shopId(Long.parseLong(String.valueOf(map.get("shopId"))))
                .itemId(Long.parseLong(String.valueOf(map.get("itemId"))))
                .quantity(Long.parseLong(String.valueOf(map.get("quantity"))))
                .build();
    }

    public static OrderItemRequest forTest(Long shopId, Long itemId, Long quantity) {
        return OrderItemRequest.builder()
                .shopId(shopId)
                .itemId(itemId)
                .quantity(quantity)
                .build();
    }
}
