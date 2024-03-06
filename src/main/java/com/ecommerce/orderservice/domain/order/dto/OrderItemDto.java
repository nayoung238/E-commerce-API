package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemDto {

    private Long id;

    @NotNull
    private Long itemId;

    @NotNull
    private Long quantity;

    private OrderStatus orderStatus;

    public static OrderItemDto of(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .orderStatus((orderItem.getOrderStatus() == null) ? OrderStatus.WAITING : orderItem.getOrderStatus())
                .build();
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
