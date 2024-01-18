package com.nayoung.itemservice.kafka.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private String eventId;
    private OrderItemStatus orderStatus;
    private List<OrderItemDto> orderItemDtos;
    private Long customerAccountId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public void setOrderStatus(OrderItemStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOrderItemDtos(List<OrderItemDto> orderItemDtos) {
        this.orderItemDtos = orderItemDtos;
    }
}
