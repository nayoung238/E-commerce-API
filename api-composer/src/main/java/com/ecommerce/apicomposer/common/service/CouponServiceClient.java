package com.ecommerce.apicomposer.common.service;

import com.ecommerce.apicomposer.mypage.dto.CouponResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "COUPON-SERVICE")
public interface CouponServiceClient {

	@GetMapping("/coupons/log")
	List<CouponResponseDto> findCouponList(@RequestHeader("X-User-Id") Long userId);
}
