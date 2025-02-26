package com.ecommerce.apicomposer.mypage.dto;

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
    List<OrderSimpleDto> orders,

    @JsonProperty("coupons")
    List<CouponResponseDto> coupons
) {

    public static MyPageResponseDto of(AccountResponseDto account, List<OrderSimpleDto> orders, List<CouponResponseDto> coupons) {
        return MyPageResponseDto.builder()
                .accountId(account.accountId())
                .loginId(account.loginId())
                .name(account.name())
                .orders(orders)
                .coupons(coupons)
                .build();
    }
}
