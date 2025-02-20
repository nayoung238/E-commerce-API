package com.ecommerce.couponservice.couponlog.dto;

import com.ecommerce.couponservice.couponlog.entity.CouponLog;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CouponLogResponseDto (

	String couponName,
	BigDecimal discountRate
) {

	public static CouponLogResponseDto of(CouponLog couponLog) {
		return CouponLogResponseDto.builder()
			.couponName(couponLog.getCoupon().getName())
			.discountRate(couponLog.getCoupon().getDiscountRate())
			.build();
	}
}
