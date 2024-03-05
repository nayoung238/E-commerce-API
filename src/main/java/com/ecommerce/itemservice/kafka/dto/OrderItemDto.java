package com.ecommerce.itemservice.kafka.dto;

import lombok.*;

@Getter
@Builder
public class OrderItemDto {

    private Long itemId;
    private Long quantity;
    private OrderItemStatus orderItemStatus;

    public void convertSign() {
        quantity *= -1;
    }

    public void updateOrderStatus(OrderItemStatus orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }
}
