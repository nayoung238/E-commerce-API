package com.ecommerce.apicomposer.common.service;

import com.ecommerce.apicomposer.mypage.dto.AccountResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface AccountServiceClient {

	@GetMapping("/accounts/{accountId}")
	AccountResponseDto findAccount(@PathVariable Long accountId);
}
