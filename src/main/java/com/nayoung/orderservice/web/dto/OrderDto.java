package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto implements Serializable {

    private Long id;

    private String eventId;

    private OrderItemStatus orderStatus;

    @NotNull
    @Valid
    private List<OrderItemDto> orderItemDtos;

    @NotNull
    @Min(value = 1)
    private Long customerAccountId;

    private LocalDateTime createdAt;

    private LocalDateTime requestedAt;  // item-service에서 주문 이벤트 중복 처리를 판별하기 위한 redis key

    public static OrderDto fromOrder(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().parallelStream()
                .map(OrderItemDto::fromOrderItem)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .eventId(order.getEventId())
                .orderStatus((order.getOrderStatus() == null) ? OrderItemStatus.WAITING : order.getOrderStatus())
                .orderItemDtos(orderItemDtos)
                .customerAccountId(order.getCustomerAccountId())
                .createdAt(order.getCreatedAt())
                .requestedAt(order.getRequestedAt())
                .build();
    }

    public void setOrderStatus(OrderItemStatus orderItemStatus) {
        this.orderStatus = orderItemStatus;
    }

    public void initializeRequestedAt() {
        this.requestedAt = LocalDateTime.now();
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
