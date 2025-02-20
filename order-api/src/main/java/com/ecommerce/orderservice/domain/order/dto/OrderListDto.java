package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import lombok.Builder;

import java.util.Comparator;
import java.util.List;

@Builder
public record OrderListDto (

    long accountId,
    List<OrderSimpleDto> orderSimpleDtoList
) {

    public static OrderListDto of(long accountId, List<Order> orders) {
        List<OrderSimpleDto> orderSimpleDtoList =  orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderSimpleDto::of)
                .toList();

        return OrderListDto.builder()
                .accountId(accountId)
                .orderSimpleDtoList(orderSimpleDtoList)
                .build();
    }
}
