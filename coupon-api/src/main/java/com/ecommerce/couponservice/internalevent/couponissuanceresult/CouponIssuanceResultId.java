package com.ecommerce.couponservice.internalevent.couponissuanceresult;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
public class CouponIssuanceResultId implements Serializable {

    @NotNull
    @Column(name = "1_coupon_id", nullable = false)
    private Long couponId;

    @NotNull
    @Column(name = "2_account_id", nullable = false)
    private Long accountId;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponIssuanceResultId(long couponId, long accountId) {
        this.couponId = couponId;
        this.accountId = accountId;
    }

    public static CouponIssuanceResultId of(long couponId, long accountId) {
        return CouponIssuanceResultId.builder()
                .couponId(couponId)
                .accountId(accountId)
                .build();
    }
}

