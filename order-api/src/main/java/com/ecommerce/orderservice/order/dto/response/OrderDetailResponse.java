package com.ecommerce.orderservice.order.dto.response;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderStatus;
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
    private OrderStatus orderStatus;
    private final List<OrderItemResponse> orderItems;
    private final LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderDetailResponse(Long id, String orderEventId, Long userId,
								OrderStatus orderStatus, List<OrderItemResponse> orderItemResponses,
								LocalDateTime requestedAt) {
        this.id = id;
        this.orderEventId = orderEventId;
        this.userId = userId;
        this.orderStatus = orderStatus;
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
                .orderStatus((order.getOrderStatus() == null) ? OrderStatus.PROCESSING : order.getOrderStatus())
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
                .orderStatus(orderKafkaEvent.getOrderStatus())
                .orderItemResponses(orderItems)
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
