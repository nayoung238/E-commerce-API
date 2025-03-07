package com.ecommerce.auth.auth.api;

import com.ecommerce.auth.auth.dto.request.LoginRequest;
import com.ecommerce.auth.auth.service.AuthService;
import com.ecommerce.auth.common.config.SecurityConfig;
import com.ecommerce.auth.user.dto.request.SignUpRequest;
import com.ecommerce.auth.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	UserService userService;
	@Autowired
	private AuthService authService;

	@Test
	@DisplayName("[로그인 성공 테스트] 로그인 성공 시 헤더와 쿠키에 각 토큰 추가")
	void login_success_token_test() throws Exception {
		// given
		final String loginId = "test-login-id";
		final String password = "test-password";

		SignUpRequest signUpRequest = SignUpRequest.builder()
			.loginId(loginId)
			.name("test-name")
			.password(password)
			.build();

		userService.createUser(signUpRequest);

		LoginRequest loginRequest = LoginRequest.builder()
			.loginId(loginId)
			.password(password)
			.build();

		// when & then
		mockMvc.perform(
			post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(header().exists("Authorization"))
			.andExpect(header().string("Authorization", Matchers.startsWith("Bearer ")))
			.andExpect(cookie().exists("refresh_token"))
			.andDo(print());
	}

	@Test
	@DisplayName("[로그인 실패 테스트] 로그인 시 로그인 아이디, 비밀번호 필수")
	void login_failed_test_when_input_invalid () throws Exception {
		// given
		final String loginId = null;
		final String password = "test-password";

		LoginRequest loginRequest = LoginRequest.builder()
			.loginId(loginId)
			.password(password)
			.build();

		// when & then
		mockMvc.perform(
			post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("로그인 아이디는 필수입니다.")))
			.andDo(print());
	}
}