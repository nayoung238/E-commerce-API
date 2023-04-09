package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.log.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemResponse {

    private OrderStatus orderStatus;
    private Long shopId;
    private Long itemId;
    private Long quantity;

    @Builder
    private OrderItemResponse(OrderStatus orderStatus, Long shopId, Long itemId, Long quantity) {
        this.orderStatus = orderStatus;
        this.shopId = shopId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public static OrderItemResponse fromOrderItemRequest(OrderStatus orderStatus, OrderItemRequest request) {
        return OrderItemResponse.builder()
                .orderStatus(orderStatus)
                .shopId(request.getShopId())
                .itemId(request.getItemId())
                .quantity(request.getQuantity())
                .build();
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
