package com.ecommerce.apigatewayservice.service.mypage.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class OrderListDto {

    List<OrderSimpleDto> orderSimpleDtoList;

    public OrderListDto(List<OrderSimpleDto> s) {
    }
}
