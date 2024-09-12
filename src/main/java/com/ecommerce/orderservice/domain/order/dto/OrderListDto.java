package com.ecommerce.orderservice.domain.order.dto;

import com.ecommerce.orderservice.domain.order.Order;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

@Getter
public class OrderListDto {

    private final long accountId;
    private final List<OrderSimpleDto> orderSimpleDtoList;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderListDto(long accountId, List<OrderSimpleDto> orderSimpleDtoList) {
        this.accountId = accountId;
        this.orderSimpleDtoList = orderSimpleDtoList;
    }

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
