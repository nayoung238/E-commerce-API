package com.ecommerce.apicomposer.mypage.dto.response;

import java.math.BigDecimal;

public record CouponLogResponse(

	String couponName,
	BigDecimal discountRate
) {
}
