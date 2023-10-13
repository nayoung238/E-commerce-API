package com.nayoung.itemservice.messagequeue.client;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private Long customerAccountId;
    private List<OrderItemDto> orderItemDtos;
    private OrderItemStatus orderStatus;
    private Long totalPrice;
    private LocalDateTime createdAt;
}
