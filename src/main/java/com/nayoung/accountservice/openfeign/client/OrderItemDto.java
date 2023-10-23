package com.nayoung.accountservice.openfeign.client;

import lombok.Getter;

@Getter
public class OrderItemDto {
    private Long id;
    private Long itemId;
    private Long quantity;
    private Long price;
    private Long shopId;
    private OrderItemStatus orderItemStatus;
}
