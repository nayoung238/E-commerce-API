package com.ecommerce.apigatewayservice.service.mypage.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class OrderListDto {

    private Long accountId;
    private List<OrderSimpleDto> orderSimpleDtoList;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderListDto(Long accountId, List<OrderSimpleDto> orderSimpleDtoList) {
        this.accountId = accountId;
        this.orderSimpleDtoList = orderSimpleDtoList;
    }

    public static OrderListDto emptyInstance() {
        return OrderListDto.builder()
                .accountId(null)
                .orderSimpleDtoList(null)
                .build();
    }
}
