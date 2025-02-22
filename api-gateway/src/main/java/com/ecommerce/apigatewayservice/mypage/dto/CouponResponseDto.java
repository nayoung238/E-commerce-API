package com.ecommerce.apigatewayservice.mypage.dto;

import java.math.BigDecimal;

public record CouponResponseDto (

	String couponName,
	BigDecimal discountRate
) {
}
