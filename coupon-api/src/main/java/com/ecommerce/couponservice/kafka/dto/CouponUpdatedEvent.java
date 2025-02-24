package com.ecommerce.couponservice.kafka.dto;

import com.ecommerce.couponservice.coupon.enums.CouponStatus;

import java.math.BigDecimal;

public record CouponUpdatedEvent (
	long accountId,
	String couponName,
	BigDecimal discountRate,
	CouponStatus couponStatus
) { }
