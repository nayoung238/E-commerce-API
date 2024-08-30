package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderDto {

    private Long id;

    private String orderEventId;

    @Setter
    private OrderStatus orderStatus;

    private List<OrderItemDto> orderItemDtos;

    private Long accountId;

    private LocalDateTime createdAt;

    private LocalDateTime requestedAt;  // item-service에서 주문 이벤트 중복 처리를 판별하기 위한 redis key

    public static OrderDto of(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems()
                .parallelStream()
                .map(OrderItemDto::of)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderEventId(order.getOrderEventId())
                .orderStatus((order.getOrderStatus() == null) ? OrderStatus.WAITING : order.getOrderStatus())
                .orderItemDtos(orderItemDtos)
                .accountId(order.getAccountId())
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
                .orderStatus(orderKafkaEvent.getOrderStatus())
                .orderItemDtos(orderItemDtos)
                .accountId(orderKafkaEvent.getAccountId())
                .createdAt(null)
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();
    }
}
