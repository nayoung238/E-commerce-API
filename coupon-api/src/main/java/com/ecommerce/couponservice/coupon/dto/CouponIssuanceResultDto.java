package com.ecommerce.couponservice.coupon.dto;

import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouponIssuanceResultDto {

    private CouponIssuanceStatus status;
    private Long couponId;
    private Long userId;
    private String message;

    public static CouponIssuanceResultDto of(Long couponId, Long userId, CouponIssuanceStatus status) {
        return CouponIssuanceResultDto.builder()
                .status(status)
                .couponId(couponId)
                .userId(userId)
                .message(status.getMessage())
                .build();
    }
}

