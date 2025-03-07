package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderItemDto;
import com.ecommerce.orderservice.order.dto.OrderItemRequestDto;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderItemKafkaEvent {

    private Long itemId;
    private Long quantity;
    private OrderProcessingStatus orderProcessingStatus;

    public static OrderItemKafkaEvent of(OrderItem orderItem) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .orderProcessingStatus(orderItem.getOrderProcessingStatus() != null ? orderItem.getOrderProcessingStatus() : null)
                .build();
    }

    public static OrderItemKafkaEvent of(OrderItemDto orderItemDto) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .orderProcessingStatus(orderItemDto.getOrderProcessingStatus() != null ? orderItemDto.getOrderProcessingStatus() : null)
                .build();
    }

    public static OrderItemKafkaEvent of(OrderItemRequestDto orderItemRequestDto) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemRequestDto.itemId())
                .quantity(orderItemRequestDto.quantity())
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
                .build();
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
