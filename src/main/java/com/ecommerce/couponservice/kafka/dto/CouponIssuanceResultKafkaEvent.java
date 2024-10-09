package com.ecommerce.couponservice.kafka.dto;

import com.ecommerce.couponservice.internalevent.couponissuanceresult.CouponIssuanceResultId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CouponIssuanceResultKafkaEvent {

    private long couponId;
    private long accountId;
    private String couponName;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponIssuanceResultKafkaEvent(long couponId, long accountId, String couponName) {
        this.couponId = couponId;
        this.accountId = accountId;
        this.couponName = couponName;
    }

    public static CouponIssuanceResultKafkaEvent of(CouponIssuanceResultId id, String couponName) {
        return CouponIssuanceResultKafkaEvent.builder()
                .couponId(id.getCouponId())
                .accountId(id.getAccountId())
                .couponName(couponName)
                .build();
    }

    public static CouponIssuanceResultKafkaEvent of(Long couponId, Long accountId, String couponName) {
        return CouponIssuanceResultKafkaEvent.builder()
                .couponId(couponId)
                .accountId(accountId)
                .couponName(couponName)
                .build();
    }
}
