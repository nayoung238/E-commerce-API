package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderItem;
import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSimpleDto (

    long orderId,
    String orderEventId,
    String orderName,
    OrderProcessingStatus orderStatus,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime requestedAt
) {

    public static OrderSimpleDto of(Order order) {
        return OrderSimpleDto.builder()
                .orderId(order.getId())
                .orderEventId(order.getOrderEventId())
                .orderName(getOrderName(order.getOrderItems()))
                .orderStatus(order.getOrderProcessingStatus())
                .requestedAt(order.getRequestedAt())
                .build();
    }

    private static String getOrderName(List<OrderItem> orderItemList) {
        if(orderItemList.size() == 1) return "테스트 아이템";
        return "테스트 아이템 외 " + (orderItemList.size() - 1) + "건";
    }
}
