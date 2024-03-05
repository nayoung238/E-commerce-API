package com.ecommerce.itemservice.kafka.dto;

import lombok.*;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private Long quantity;
    @Setter
    private OrderItemStatus orderItemStatus;

    public void convertSign() {
        quantity *= -1;
    }

    public void updateOrderStatus(OrderItemStatus orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }
}
