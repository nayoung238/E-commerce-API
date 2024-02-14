package com.ecommerce.accountservice.openfeign.client;

import lombok.Getter;

@Getter
public class OrderItemDto {
    private Long id;
    private Long itemId;
    private Long quantity;
    private OrderItemStatus orderItemStatus;
}
