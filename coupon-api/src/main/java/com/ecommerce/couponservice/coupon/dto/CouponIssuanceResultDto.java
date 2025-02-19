package com.ecommerce.couponservice.coupon.dto;

import com.ecommerce.couponservice.redis.manager.CouponIssuanceStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponIssuanceResultDto {

    private CouponIssuanceStatus status;
    private Long couponId;
    private Long accountId;
    private String message;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponIssuanceResultDto(CouponIssuanceStatus status, Long couponId, Long accountId, String message) {
        this.status = status;
        this.couponId = couponId;
        this.accountId = accountId;
        this.message = message;
    }

    public static CouponIssuanceResultDto of(Long couponId, Long accountId, CouponIssuanceStatus status) {
        return CouponIssuanceResultDto.builder()
                .status(status)
                .couponId(couponId)
                .accountId(accountId)
                .message(status.getMessage())
                .build();
    }
}

