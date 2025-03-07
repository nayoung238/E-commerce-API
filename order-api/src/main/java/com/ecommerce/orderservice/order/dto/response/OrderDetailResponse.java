package com.ecommerce.orderservice.order.dto.response;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDetailResponse {

    private final Long id;
    private final String orderEventId;
    private final Long userId;
    private OrderProcessingStatus orderProcessingStatus;
    private final List<OrderItemResponse> orderItems;
    private final LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderDetailResponse(Long id, String orderEventId, Long userId,
                                OrderProcessingStatus orderProcessingStatus, List<OrderItemResponse> orderItemResponses,
                                LocalDateTime requestedAt) {
        this.id = id;
        this.orderEventId = orderEventId;
        this.userId = userId;
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItems = orderItemResponses;
        this.requestedAt = requestedAt;
    }

    public static OrderDetailResponse of(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems()
                .parallelStream()
                .map(OrderItemResponse::of)
                .collect(Collectors.toList());

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderEventId(order.getOrderEventId())
                .userId(order.getUserId())
                .orderProcessingStatus((order.getOrderProcessingStatus() == null) ? OrderProcessingStatus.PROCESSING : order.getOrderProcessingStatus())
                .orderItemResponses(orderItems)
                .requestedAt(order.getRequestedAt())
                .build();
    }

    public static OrderDetailResponse of(OrderKafkaEvent orderKafkaEvent) {
        List<OrderItemResponse> orderItems = orderKafkaEvent
                .getOrderItemKafkaEvents()
                .parallelStream()
                .map(OrderItemResponse::of)
                .toList();

        return OrderDetailResponse.builder()
                .id(null)
                .orderEventId(orderKafkaEvent.getOrderEventId())
                .userId(orderKafkaEvent.getUserId())
                .orderProcessingStatus(orderKafkaEvent.getOrderProcessingStatus())
                .orderItemResponses(orderItems)
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
