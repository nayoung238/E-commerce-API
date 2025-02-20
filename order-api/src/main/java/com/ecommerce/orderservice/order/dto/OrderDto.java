package com.ecommerce.orderservice.order.dto;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDto {

    private final Long id;
    private final String orderEventId;
    private final Long accountId;
    private OrderProcessingStatus orderProcessingStatus;
    private final List<OrderItemDto> orderItemDtos;
    private final LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderDto(Long id, String orderEventId, Long accountId,
                     OrderProcessingStatus orderProcessingStatus, List<OrderItemDto> orderItemDtos,
                     LocalDateTime requestedAt) {
        this.id = id;
        this.orderEventId = orderEventId;
        this.accountId = accountId;
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItemDtos = orderItemDtos;
        this.requestedAt = requestedAt;
    }

    public static OrderDto of(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems()
                .parallelStream()
                .map(OrderItemDto::of)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderEventId(order.getOrderEventId())
                .accountId(order.getAccountId())
                .orderProcessingStatus((order.getOrderProcessingStatus() == null) ? OrderProcessingStatus.PROCESSING : order.getOrderProcessingStatus())
                .orderItemDtos(orderItemDtos)
                .requestedAt(order.getRequestedAt())
                .build();
    }

    public static OrderDto of(OrderKafkaEvent orderKafkaEvent) {
        List<OrderItemDto> orderItemDtos = orderKafkaEvent
                .getOrderItemKafkaEvents()
                .parallelStream()
                .map(OrderItemDto::of)
                .toList();

        return OrderDto.builder()
                .id(null)
                .orderEventId(orderKafkaEvent.getOrderEventId())
                .accountId(orderKafkaEvent.getAccountId())
                .orderProcessingStatus(orderKafkaEvent.getOrderProcessingStatus())
                .orderItemDtos(orderItemDtos)
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
