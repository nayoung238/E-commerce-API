package com.nayoung.orderservice.messagequeue.openFeign;

import com.nayoung.orderservice.domain.OrderItemStatus;
import lombok.Getter;

@Getter
public class ItemUpdateLogDto {

    private Long id;
    private Long eventId;
    private OrderItemStatus orderItemStatus;
    private Long itemId;
    private Long quantity;
}