package com.ecommerce.apicomposer.mypage.api;

import com.ecommerce.apicomposer.mypage.dto.response.MyPageResponse;
import com.ecommerce.apicomposer.mypage.service.MyPageCqrsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my-page")
@RequiredArgsConstructor
public class MyPageController {

	private final MyPageCqrsService myPageCqrsService;

	@GetMapping
	public ResponseEntity<?> findMyPage(HttpServletRequest httpServletRequest) {
		MyPageResponse response = myPageCqrsService.getMyPage(httpServletRequest);
		return ResponseEntity.ok(response);
	}
}
