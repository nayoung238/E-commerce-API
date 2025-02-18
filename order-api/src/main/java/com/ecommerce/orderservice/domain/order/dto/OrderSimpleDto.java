package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderItem;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderSimpleDto {

    private final Long orderId;
    private final String orderEventId;
    private final String orderName;
    private final String orderStatus;
    private final LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderSimpleDto(long orderId, String orderEventId, String orderName, String orderStatus, LocalDateTime requestedAt) {
        this.orderId = orderId;
        this.orderEventId = orderEventId;
        this.orderName = orderName;
        this.orderStatus = orderStatus;
        this.requestedAt = requestedAt;
    }

    public static OrderSimpleDto of(Order order) {
        return OrderSimpleDto.builder()
                .orderId(order.getId())
                .orderEventId(order.getOrderEventId())
                .orderName(getOrderName(order.getOrderItems()))
                .orderStatus(String.valueOf(order.getOrderProcessingStatus()))
                .requestedAt(order.getRequestedAt())
                .build();
    }

    private static String getOrderName(List<OrderItem> orderItemList) {
        if(orderItemList.size() == 1) return "테스트 아이템";
        return "테스트 아이템 외 " + (orderItemList.size() - 1) + "건";
    }
}
