package com.ecommerce.apicomposer.mypage.dto;

import java.math.BigDecimal;

public record CouponResponseDto (

	String couponName,
	BigDecimal discountRate
) {
}
