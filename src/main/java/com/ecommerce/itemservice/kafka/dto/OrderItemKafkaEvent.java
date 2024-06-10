package com.ecommerce.itemservice.kafka.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemKafkaEvent {

    private Long itemId;
    private Long quantity;
    private OrderStatus orderStatus;

    public void convertSign() {
        quantity *= -1;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
