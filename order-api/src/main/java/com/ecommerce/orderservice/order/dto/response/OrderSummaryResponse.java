package com.ecommerce.orderservice.order.dto.response;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSummaryResponse(

    Long orderId,
    String orderEventId,
    String orderName,
    OrderStatus orderStatus,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    LocalDateTime requestedAt
) {

    public static OrderSummaryResponse of(Order order) {
        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderEventId(order.getOrderEventId())
                .orderName(getOrderName(order.getOrderItems()))
                .orderStatus(order.getOrderStatus())
                .requestedAt(order.getRequestedAt())
                .build();
    }

    private static String getOrderName(List<OrderItem> orderItemList) {
        if(orderItemList.size() == 1) return "테스트 아이템";
        return "테스트 아이템 외 " + (orderItemList.size() - 1) + "건";
    }
}
