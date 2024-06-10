package com.ecommerce.orderservice.domain.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderListDto {

    List<OrderSimpleDto> orderSimpleDtoList;

    public OrderListDto(List<OrderSimpleDto> orderSimpleDtoList) {
        this.orderSimpleDtoList = orderSimpleDtoList;
    }
}
