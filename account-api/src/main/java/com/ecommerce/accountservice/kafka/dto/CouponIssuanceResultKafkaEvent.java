package com.ecommerce.accountservice.kafka.dto;

import lombok.Getter;

@Getter
public class CouponIssuanceResultKafkaEvent {

    private long couponId;
    private long accountId;
    private String couponName;
}
