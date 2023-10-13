package com.nayoung.itemservice.messagequeue.client;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private Long quantity;
    private Long price;
    private Long shopId;
    private OrderItemStatus orderItemStatus;
}
