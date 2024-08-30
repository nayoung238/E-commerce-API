package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static OrderItemKafkaEvent of(OrderItemDto orderItemDto) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .orderStatus(orderItemDto.getOrderStatus() != null ? orderItemDto.getOrderStatus() : null)
                .build();
    }

    public static OrderItemKafkaEvent of(OrderItemRequestDto orderItemRequestDto) {
        return OrderItemKafkaEvent.builder()
                .itemId(orderItemRequestDto.getItemId())
                .quantity(orderItemRequestDto.getQuantity())
                .orderStatus(OrderStatus.WAITING)
                .build();
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
