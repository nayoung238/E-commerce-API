package com.ecommerce.apicomposer.common.service;

import com.ecommerce.apicomposer.mypage.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

	@GetMapping("/users/{userId}")
	UserResponse findUser(@PathVariable Long userId);
}
