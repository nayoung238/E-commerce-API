package com.ecommerce.apigatewayservice.service.mypage.dto;

import java.math.BigDecimal;

public record CouponResponseDto (

	String couponName,
	BigDecimal discountRate
) {
}
