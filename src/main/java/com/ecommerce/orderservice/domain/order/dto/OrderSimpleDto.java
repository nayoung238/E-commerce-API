package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderSimpleDto {

    private String orderEventId;
    private String orderName;
    private String orderStatus;
    private LocalDateTime requestedAt;

    public static OrderSimpleDto of(Order order) {
        return OrderSimpleDto.builder()
                .orderEventId(order.getOrderEventId())
                .orderName(getOrderName(order.getOrderItems()))
                .orderStatus(String.valueOf(order.getOrderStatus()))
                .requestedAt(order.getRequestedAt())
                .build();
    }

    private static String getOrderName(List<OrderItem> orderItemList) {
        if(orderItemList.size() == 1) return "테스트 아이템";
        return "테스트 아이템 외 " + (orderItemList.size() - 1) + "건";
    }
}
