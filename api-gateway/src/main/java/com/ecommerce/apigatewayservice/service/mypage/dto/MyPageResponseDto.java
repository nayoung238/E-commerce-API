package com.ecommerce.apigatewayservice.service.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record MyPageResponseDto (

    @JsonProperty("accountId")
    Long accountId,

    @JsonProperty("loginId")
    String loginId,

    @JsonProperty("name")
    String name,

    @JsonProperty("orders")
    List<OrderSimpleDto> orders
) {

    public static MyPageResponseDto of(AccountResponseDto account, OrderListDto orders) {
        return MyPageResponseDto.builder()
                .accountId(account.accountId())
                .loginId(account.loginId())
                .name(account.name())
                .orders(orders.getOrderSimpleDtoList())
                .build();
    }
}
