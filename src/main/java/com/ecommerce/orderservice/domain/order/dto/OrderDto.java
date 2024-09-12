package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
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
    private final LocalDateTime createdAt;
    private final LocalDateTime requestedAt;  // item-service에서 주문 이벤트 중복 처리를 판별하기 위한 redis key

    @Builder(access = AccessLevel.PRIVATE)
    private OrderDto(Long id, String orderEventId, Long accountId,
                     OrderProcessingStatus orderProcessingStatus, List<OrderItemDto> orderItemDtos,
                     LocalDateTime createdAt, LocalDateTime requestedAt) {
        this.id = id;
        this.orderEventId = orderEventId;
        this.accountId = accountId;
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItemDtos = orderItemDtos;
        this.createdAt = createdAt;
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
                .createdAt(order.getCreatedAt())
                .requestedAt(order.getRequestedAt())
                .build();
    }

    /**
     * Stream-KTable Join으로 주문 생성하는 방식에서 사용
     * @param orderKafkaEvent DB insert 전 (OrderDto.id, OrderDto.createdAt null로 설정)
     * @return
     */
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
                .createdAt(null)
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
