package com.nayoung.orderservice.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequest {

    private Long orderId;
    private Long customerAccountId;
    private List<OrderItemRequest> orderItems;

    @Builder
    public OrderRequest(Long customerAccountId, List<OrderItemRequest> orderItems) {
        this.customerAccountId = customerAccountId;
        this.orderItems = orderItems;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setCustomerAccountId(Long customerAccountId) {
        this.customerAccountId = customerAccountId;
    }
}
