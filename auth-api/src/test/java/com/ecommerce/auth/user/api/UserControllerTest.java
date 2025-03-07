package com.ecommerce.auth.user.api;

import com.ecommerce.auth.user.dto.request.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	@DisplayName("[회원 가입 성공 테스트] 회원 가입 성공 시 회원 정보 반환")
	void sign_up_succeed_test () throws Exception {
		// given
		SignUpRequest request = SignUpRequest.builder()
			.loginId("test-login-id")
			.password("test-password")
			.name("test-name")
			.build();

		// when & then
		mockMvc.perform(
			post("/users/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.loginId").value(request.loginId()))
			.andExpect(jsonPath("$.name").value(request.name()))
			.andDo(print());
	}

	@Test
	@DisplayName("[회원 가입 실패 테스트] 요청 DTO 유효성 위반")
	void sign_up_failure_test () throws Exception {
		// given
		SignUpRequest request = SignUpRequest.builder()
			.loginId(" ")
			.password("test-password")
			.name("test-name")
			.build();

		// when & then
		mockMvc.perform(
				post("/users/sign-up")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("로그인 아이디는 필수입니다.")))
			.andDo(print());
	}
}