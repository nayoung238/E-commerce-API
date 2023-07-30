package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.OrderItem;
import com.nayoung.orderservice.domain.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter @Builder
public class OrderItemDto {

    private Long orderItemId;

    @NotBlank
    private Long itemId;

    @NotBlank
    private Long quantity;

    @NotBlank
    private Long price;

    @NotBlank
    private Long shopId;

    private OrderStatus orderStatus;

    public static OrderItemDto fronmOrderItem(OrderItem orderItem) {
        return OrderItemDto.builder()
                .orderItemId(orderItem.getId())
                .itemId(orderItem.getItemId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .shopId(orderItem.getShopId())
                .build();
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
