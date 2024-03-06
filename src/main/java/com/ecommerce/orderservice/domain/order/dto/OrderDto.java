package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderDto {

    private Long id;

    private String orderEventKey;

    @Setter
    private OrderStatus orderStatus;

    @Valid
    private List<OrderItemDto> orderItemDtos;

    @NotNull
    @Min(value = 1)
    private Long userId;

    private LocalDateTime createdAt;

    private LocalDateTime requestedAt;  // item-service에서 주문 이벤트 중복 처리를 판별하기 위한 redis key

    public static OrderDto of(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems()
                .parallelStream()
                .map(OrderItemDto::of)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderEventKey(order.getOrderEventKey())
                .orderStatus((order.getOrderStatus() == null) ? OrderStatus.WAITING : order.getOrderStatus())
                .orderItemDtos(orderItemDtos)
                .userId(order.getUserId())
                .createdAt(order.getCreatedAt())
                .requestedAt(order.getRequestedAt())
                .build();
    }

    public void initializeOrderEventKey(String orderEventKey) {
        this.orderEventKey = orderEventKey;
    }

    public void initializeRequestedAt() {
        this.requestedAt = LocalDateTime.now();
    }
}
