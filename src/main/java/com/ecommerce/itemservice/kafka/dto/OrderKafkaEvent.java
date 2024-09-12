package com.ecommerce.itemservice.kafka.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderKafkaEvent {

    private String orderEventId;
    private Long accountId;
    private OrderProcessingStatus orderProcessingStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderKafkaEvent(String orderEventId, long accountId,
                            OrderProcessingStatus orderProcessingStatus,
                            List<OrderItemKafkaEvent> orderItemKafkaEvents,
                            LocalDateTime createdAt, LocalDateTime requestedAt) {
        this.orderEventId = orderEventId;
        this.accountId = accountId;
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItemKafkaEvents = orderItemKafkaEvents;
        this.createdAt = createdAt;
        this.requestedAt = requestedAt;
    }

    public static OrderKafkaEvent of(String orderEventId, long accountId,
                                     OrderProcessingStatus orderProcessingStatus,
                                     List<OrderItemKafkaEvent> orderItemKafkaEvents,
                                     LocalDateTime createdAt, LocalDateTime requestedAt) {
        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .accountId(accountId)
                .orderProcessingStatus(orderProcessingStatus)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .createdAt(createdAt)
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
