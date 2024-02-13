package com.ecommerce.itemservice.kafka.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private String eventId;
    private OrderItemStatus orderStatus;
    @Setter
    private List<OrderItemDto> orderItemDtos;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public void updateOrderStatus(OrderItemStatus status) {
        this.orderStatus = status;
        this.orderItemDtos
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }
}
