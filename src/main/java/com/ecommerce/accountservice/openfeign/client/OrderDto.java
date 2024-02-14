package com.ecommerce.accountservice.openfeign.client;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderDto {
    private Long id;
    private String eventId;
    private OrderItemStatus orderStatus;
    private List<OrderItemDto> orderItemDtos;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;
}
