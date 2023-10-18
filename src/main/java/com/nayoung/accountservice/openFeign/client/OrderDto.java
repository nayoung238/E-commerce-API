package com.nayoung.accountservice.openFeign.client;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderDto {
    private Long id;
    private String eventId;
    private OrderItemStatus orderStatus;
    private List<OrderItemDto> orderItemDtos;
    private Long customerAccountId;
    private Long totalPrice;
    private LocalDateTime createdAt;
}
