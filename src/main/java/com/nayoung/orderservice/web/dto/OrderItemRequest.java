package com.nayoung.orderservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemRequest {

    private Long shopId;
    private Long itemId;
    private Long quantity;
    private Long price;

    @Builder
    public OrderItemRequest(Long shopId, Long itemId, Long quantity, Long price) {
        this.shopId = shopId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.price = price;
    }
}
