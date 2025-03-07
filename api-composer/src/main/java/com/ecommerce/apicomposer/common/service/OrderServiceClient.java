package com.ecommerce.apicomposer.common.service;

import com.ecommerce.apicomposer.auth.jwt.JwtUtil;
import com.ecommerce.apicomposer.mypage.dto.response.OrderSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient("ORDER-SERVICE")
public interface OrderServiceClient {

	@GetMapping("/orders/")
	List<OrderSummaryResponse> findOrders(@RequestHeader(JwtUtil.HEADER_AUTHORIZATION) String authorization);
}
