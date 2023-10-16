package com.nayoung.itemservice.messagequeue.client;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long id;
    private Long customerAccountId;
    private OrderItemStatus orderStatus;
    private List<OrderItemDto> orderItemDtos;
    private Long totalPrice;
    private LocalDateTime createdAt;

    public void setOrderStatus(OrderItemStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
