package com.nayoung.orderservice.messagequeue.client;

import com.nayoung.orderservice.domain.OrderItemStatus;
import lombok.Getter;

@Getter
public class ItemUpdateLogDto {

    private Long id;
    private OrderItemStatus orderItemStatus;
    private Long orderId;
    private Long customerAccountId;
    private Long itemId;
    private Long quantity;
}