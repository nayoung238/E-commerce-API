package com.ecommerce.itemservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private String orderEventKey;
    private OrderStatus orderStatus;
    private List<OrderItemEvent> orderItemEvents;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public void updateOrderItemDtos(List<OrderItemEvent> orderItemEvents) {
        this.orderItemEvents = orderItemEvents;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
