package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ItemStockUpdateRequest {

    private Long orderId;
    private Long customerAccountId;
    private List<OrderItemRequest> orderItemRequests;

    @Builder
    private ItemStockUpdateRequest(Long orderId, Long customerAccountId, List<OrderItemRequest> orderItemRequests) {
        this.orderId = orderId;
        this.customerAccountId = customerAccountId;
        this.orderItemRequests = orderItemRequests;
    }

    public static ItemStockUpdateRequest fromKafkaMessage(Map<Object, Object> map, List<OrderItemRequest> orderItemRequests) {
        return ItemStockUpdateRequest.builder()
                .orderId(Long.parseLong(String.valueOf(map.get("orderId"))))
                .customerAccountId(Long.parseLong(String.valueOf(map.get("customerAccountId"))))
                .orderItemRequests(orderItemRequests)
                .build();
    }

    public static ItemStockUpdateRequest forTest(Long orderId, Long customerAccountId, List<OrderItemRequest> orderItemRequests) {
        return ItemStockUpdateRequest.builder()
                .orderId(orderId)
                .customerAccountId(customerAccountId)
                .orderItemRequests(orderItemRequests)
                .build();
    }
}
