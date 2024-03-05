package com.ecommerce.itemservice.kafka.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDto {

    private String orderId;
    private OrderItemStatus orderStatus;
    private List<OrderItemDto> orderItemDtos;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public void updateOrderItemDtos(List<OrderItemDto> orderItemDtos) {
        this.orderItemDtos = orderItemDtos;
    }

    public void updateOrderStatus(OrderItemStatus status) {
        this.orderStatus = status;
        this.orderItemDtos
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }
}
