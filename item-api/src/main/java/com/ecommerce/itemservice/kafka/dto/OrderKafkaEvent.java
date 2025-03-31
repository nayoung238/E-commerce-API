package com.ecommerce.itemservice.kafka.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderKafkaEvent {

    private String orderEventId;
    private Long userId;
    private OrderStatus orderStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderKafkaEvent(String orderEventId, long userId,
                            OrderStatus orderStatus,
                            List<OrderItemKafkaEvent> orderItemKafkaEvents,
                            LocalDateTime requestedAt) {
        this.orderEventId = orderEventId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderItemKafkaEvents = orderItemKafkaEvents;
        this.requestedAt = requestedAt;
    }

    public static OrderKafkaEvent of(String orderEventId, long userId,
                                     OrderStatus orderStatus,
                                     List<OrderItemKafkaEvent> orderItemKafkaEvents,
                                     LocalDateTime requestedAt) {
        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .userId(userId)
                .orderStatus(orderStatus)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .requestedAt(requestedAt)
                .build();
    }

    public void updateOrderItemDtos(List<OrderItemKafkaEvent> orderItemKafkaEvents) {
        this.orderItemKafkaEvents = orderItemKafkaEvents;
    }

    public void updateOrderProcessingStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
