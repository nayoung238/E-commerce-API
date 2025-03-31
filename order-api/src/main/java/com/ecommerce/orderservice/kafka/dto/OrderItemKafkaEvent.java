package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderStatus;
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
    private OrderStatus orderStatus;

    public static OrderItemKafkaEvent of(OrderItem orderItem) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .orderStatus(orderItem.getOrderStatus() != null ? orderItem.getOrderStatus() : null)
                .build();
    }

    public static OrderItemKafkaEvent of(OrderItemResponse orderItemResponse) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemResponse.getItemId())
                .quantity(orderItemResponse.getQuantity())
                .orderStatus(orderItemResponse.getOrderStatus() != null ? orderItemResponse.getOrderStatus() : null)
                .build();
    }

    public static OrderItemKafkaEvent of(OrderItemRequest orderItemRequest) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemRequest.itemId())
                .quantity(orderItemRequest.quantity())
                .orderStatus(OrderStatus.PROCESSING)
                .build();
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
