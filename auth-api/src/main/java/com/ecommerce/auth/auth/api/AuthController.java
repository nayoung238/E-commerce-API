package com.ecommerce.auth.auth.api;

import com.ecommerce.auth.auth.dto.request.LoginRequest;
import com.ecommerce.auth.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
		authService.login(loginRequest, httpServletResponse);
		return ResponseEntity.ok("로그인 성공!");
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		authService.refreshAccessToken(httpServletRequest, httpServletResponse);
		return ResponseEntity.ok("엑세스 토큰 재발급 완료!");
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		authService.logout(httpServletRequest, httpServletResponse);
		return ResponseEntity.ok("로그아웃 성공!");
	}
}
