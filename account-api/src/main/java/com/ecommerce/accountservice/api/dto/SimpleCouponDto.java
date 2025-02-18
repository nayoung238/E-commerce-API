package com.ecommerce.accountservice.api.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SimpleCouponDto {

    private final long couponId;
    private final String couponName;

    @Builder(access = AccessLevel.PRIVATE)
    private SimpleCouponDto(long couponId, String couponName) {
        this.couponId = couponId;
        this.couponName = couponName;
    }

    public static SimpleCouponDto of(long couponId, String couponName) {
        return SimpleCouponDto.builder()
                .couponId(couponId)
                .couponName(couponName)
                .build();
    }
}
