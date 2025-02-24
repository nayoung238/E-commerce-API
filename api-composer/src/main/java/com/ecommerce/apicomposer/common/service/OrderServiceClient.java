package com.ecommerce.apicomposer.common.service;

import com.ecommerce.apicomposer.mypage.dto.OrderListDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("ORDER-SERVICE")
public interface OrderServiceClient {

	@GetMapping("/orders/{accountId}")
	OrderListDto findOrderList(@PathVariable Long accountId);
}
