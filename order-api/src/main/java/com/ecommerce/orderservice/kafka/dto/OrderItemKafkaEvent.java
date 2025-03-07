package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.response.OrderItemResponse;
import com.ecommerce.orderservice.order.dto.request.OrderItemRequest;
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

    public static OrderItemKafkaEvent of(OrderItemResponse orderItemResponse) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemResponse.getItemId())
                .quantity(orderItemResponse.getQuantity())
                .orderProcessingStatus(orderItemResponse.getOrderProcessingStatus() != null ? orderItemResponse.getOrderProcessingStatus() : null)
                .build();
    }

    public static OrderItemKafkaEvent of(OrderItemRequest orderItemRequest) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemRequest.itemId())
                .quantity(orderItemRequest.quantity())
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
                .build();
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
