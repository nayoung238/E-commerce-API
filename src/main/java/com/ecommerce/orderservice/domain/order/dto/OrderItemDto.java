package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemDto {

    private Long id;

    @NotNull(message = "아이템 아이디는 필수입니다.")
    @Min(value = 1, message = "아이템 아이디는 1 이상이어야 합니다.")
    private Long itemId;

    @NotNull(message = "주문 수량은 필수입니다.")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
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
