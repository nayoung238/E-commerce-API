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
public class OrderKafkaEvent {

    private String orderEventId;
    private OrderStatus orderStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public void updateOrderItemDtos(List<OrderItemKafkaEvent> orderItemKafkaEvents) {
        this.orderItemKafkaEvents = orderItemKafkaEvents;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
