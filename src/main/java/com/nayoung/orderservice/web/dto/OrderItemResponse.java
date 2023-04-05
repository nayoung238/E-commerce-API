package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.OrderItem;
import com.nayoung.orderservice.domain.OrderStatus;
import lombok.Getter;

@Getter
public class OrderItemResponse {

    private Long orderItemId;
    private Long itemId;
    private Long quantity;
    private Long price;
    private Long shopId;
    private OrderStatus orderStatus;

    public OrderItemResponse(OrderItem orderItem) {
        this.orderItemId = orderItem.getItemId();
        this.itemId = orderItem.getItemId();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
        this.shopId = orderItem.getShopId();
        this.orderStatus = orderItem.getOrderStatus();
    }
}
