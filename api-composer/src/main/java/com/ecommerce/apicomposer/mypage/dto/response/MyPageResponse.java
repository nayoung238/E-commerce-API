package com.ecommerce.apicomposer.mypage.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record MyPageResponse (

    @JsonProperty("userId")
    Long userId,

    @JsonProperty("loginId")
    String loginId,

    @JsonProperty("name")
    String name,

    @JsonProperty("orders")
    List<OrderSummaryResponse> orders,

    @JsonProperty("coupons")
    List<CouponLogResponse> coupons
) {

    public static MyPageResponse of(UserResponse user, List<OrderSummaryResponse> orders, List<CouponLogResponse> coupons) {
        return MyPageResponse.builder()
                .userId(user.userId())
                .loginId(user.loginId())
                .name(user.name())
                .orders(orders)
                .coupons(coupons)
                .build();
    }
}
