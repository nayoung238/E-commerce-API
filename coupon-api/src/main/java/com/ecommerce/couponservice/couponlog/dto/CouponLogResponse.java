package com.ecommerce.couponservice.couponlog.dto;

import com.ecommerce.couponservice.couponlog.entity.CouponLog;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CouponLogResponse(

	String couponName,
	BigDecimal discountRate
) {

	public static CouponLogResponse of(CouponLog couponLog) {
		return CouponLogResponse.builder()
			.couponName(couponLog.getCoupon().getName())
			.discountRate(couponLog.getCoupon().getDiscountRate())
			.build();
	}
}
