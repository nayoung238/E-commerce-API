package com.ecommerce.apicomposer.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record MyPageResponseDto (

    @JsonProperty("userId")
    Long userId,

    @JsonProperty("loginId")
    String loginId,

    @JsonProperty("name")
    String name,

    @JsonProperty("orders")
    List<OrderSimpleDto> orders,

    @JsonProperty("coupons")
    List<CouponResponseDto> coupons
) {

    public static MyPageResponseDto of(UserResponseDto user, List<OrderSimpleDto> orders, List<CouponResponseDto> coupons) {
        return MyPageResponseDto.builder()
                .userId(user.userId())
                .loginId(user.loginId())
                .name(user.name())
                .orders(orders)
                .coupons(coupons)
                .build();
    }
}
