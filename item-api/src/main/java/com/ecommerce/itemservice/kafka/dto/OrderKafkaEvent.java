package com.ecommerce.itemservice.kafka.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderKafkaEvent {

    private String orderEventId;
    private Long userId;
    private OrderProcessingStatus orderProcessingStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderKafkaEvent(String orderEventId, long userId,
                            OrderProcessingStatus orderProcessingStatus,
                            List<OrderItemKafkaEvent> orderItemKafkaEvents,
                            LocalDateTime requestedAt) {
        this.orderEventId = orderEventId;
        this.userId = userId;
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItemKafkaEvents = orderItemKafkaEvents;
        this.requestedAt = requestedAt;
    }

    public static OrderKafkaEvent of(String orderEventId, long userId,
                                     OrderProcessingStatus orderProcessingStatus,
                                     List<OrderItemKafkaEvent> orderItemKafkaEvents,
                                     LocalDateTime requestedAt) {
        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .userId(userId)
                .orderProcessingStatus(orderProcessingStatus)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .requestedAt(requestedAt)
                .build();
    }

    public void updateOrderItemDtos(List<OrderItemKafkaEvent> orderItemKafkaEvents) {
        this.orderItemKafkaEvents = orderItemKafkaEvents;
    }

    public void updateOrderProcessingStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
