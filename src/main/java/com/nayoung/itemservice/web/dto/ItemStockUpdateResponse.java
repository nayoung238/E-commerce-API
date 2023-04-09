package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ItemStockUpdateResponse {

    private Boolean isAvailableToOrder;
    private Long orderId;
    private Long customerAccountId;
    private List<OrderItemResponse> orderItemResponses;

    @Builder
    private ItemStockUpdateResponse(boolean isAvailableToOrder, Long orderId, Long customerAccountId, List<OrderItemResponse> orderItemResponses) {
        this.isAvailableToOrder = isAvailableToOrder;
        this.orderId = orderId;
        this.customerAccountId = customerAccountId;
        this.orderItemResponses = orderItemResponses;
    }

    public static ItemStockUpdateResponse from(boolean isAvailableToOrder, ItemStockUpdateRequest request, List<OrderItemResponse> orderItemResponses) {
        return ItemStockUpdateResponse.builder()
                .isAvailableToOrder(isAvailableToOrder)
                .orderId(request.getOrderId())
                .customerAccountId(request.getCustomerAccountId())
                .orderItemResponses(orderItemResponses)
                .build();
    }
}
