package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemRequestDto;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderItemKafkaEvent {

    private Long itemId;
    private Long quantity;
    private OrderProcessingStatus orderProcessingStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemKafkaEvent(long itemId, long quantity, OrderProcessingStatus orderProcessingStatus) {
        this.itemId = itemId;
        this.quantity = quantity;;
        this.orderProcessingStatus = orderProcessingStatus;
    }

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
                .itemId(orderItemRequestDto.getItemId())
                .quantity(orderItemRequestDto.getQuantity())
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
                .build();
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
