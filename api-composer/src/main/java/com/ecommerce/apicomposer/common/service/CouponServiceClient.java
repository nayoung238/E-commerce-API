package com.ecommerce.apicomposer.common.service;

import com.ecommerce.apicomposer.auth.jwt.JwtUtil;
import com.ecommerce.apicomposer.mypage.dto.response.CouponLogResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "COUPON-SERVICE")
public interface CouponServiceClient {

	@GetMapping("/coupons/log")
	List<CouponLogResponse> findCoupons(@RequestHeader(JwtUtil.HEADER_AUTHORIZATION) String authorization);
}
