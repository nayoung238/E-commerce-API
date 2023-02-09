package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.Order;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderResponse {

    private Long itemId;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;

    private Long accountId;

    private LocalDateTime createdAt;

    private OrderResponse(Order order) {
        this.itemId = order.getItemId();
        this.quantity = order.getQuantity();
        this.unitPrice = order.getUnitPrice();
        this.totalPrice = order.getTotalPrice();

        this.accountId = order.getAccountId();

        this.createdAt = order.getCreatedAt();
    }

    public static OrderResponse fromOrderEntity(Order order) {
        return new OrderResponse(order);
    }
}
