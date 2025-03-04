package com.ecommerce.auth.auth.jwt;

import com.ecommerce.auth.auth.entity.RefreshToken;
import com.ecommerce.auth.auth.enums.BaseRole;
import com.ecommerce.auth.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import com.ecommerce.auth.user.dto.SignUpRequestDto;
import com.ecommerce.auth.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(JwtProperties.class)
class JwtUtilTest {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private UserService userService;

	@Test
	@DisplayName("[토큰 생성 성공 테스트] userId, role 입력 시 토큰 생성")
	void generate_token_success_test () {
		// given
		final long userId = 1L;
		final BaseRole role = BaseRole.USER;

		// when & then
		String accessToken = jwtUtil.generateAccessToken(userId, role);
		assertNotNull(accessToken);

		String refreshToken = jwtUtil.generateRefreshToken(userId, role);
		assertNotNull(refreshToken);
	}

	@Test
	@DisplayName("[토큰 생성 실패 테스트] userId 입력 필수")
	void generate_token_failed_test_when_userId_null () {
		// given
		final Long userId = null;
		final BaseRole role = BaseRole.USER;

		// when & then
		Assertions.assertThatThrownBy(() -> jwtUtil.generateAccessToken(userId, role))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException) ex;
				assertEquals(ErrorCode.MISSING_USER_ID, customException.getErrorCode());
				assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
				assertEquals("토큰 생성 시 userId 값은 필수입니다.", customException.getErrorCode().getMessage());
			});

		Assertions.assertThatThrownBy(() -> jwtUtil.generateRefreshToken(userId, role))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException) ex;
				assertEquals(ErrorCode.MISSING_USER_ID, customException.getErrorCode());
				assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
				assertEquals("토큰 생성 시 userId 값은 필수입니다.", customException.getErrorCode().getMessage());
			});
	}

	@Test
	@DisplayName("[토큰 생성 실패 테스트] role 입력 필수")
	void generate_token_failed_test_when_role_null () {
		// given
		final Long userId = 1L;
		final BaseRole role = null;

		// when & then
		Assertions.assertThatThrownBy(() -> jwtUtil.generateAccessToken(userId, role))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException) ex;
				assertEquals(ErrorCode.MISSING_ROLE, customException.getErrorCode());
				assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
				assertEquals("토큰 생성 시 role 값은 필수입니다.", customException.getErrorCode().getMessage());
			});

		Assertions.assertThatThrownBy(() -> jwtUtil.generateRefreshToken(userId, role))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException) ex;
				assertEquals(ErrorCode.MISSING_ROLE, customException.getErrorCode());
				assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
				assertEquals("토큰 생성 시 role 값은 필수입니다.", customException.getErrorCode().getMessage());
			});
	}

	@Test
	@DisplayName("[페이로드 추출 성공 테스트] 정상 토큰일 경우 userId 추출 후 반환")
	void get_userId_success_test() {
		// given
		final Long userId = 1L;
		final BaseRole role = BaseRole.USER;

		String accessToken = jwtUtil.generateAccessToken(userId, role);
		assertNotNull(accessToken);

		// when & then
		Long userIdResponse = jwtUtil.getUserIdFromToken(accessToken);
		assertEquals(userId, userIdResponse);
	}

	@Test
	@DisplayName("[페이로드 추출 실패 테스트] 비정상 토큰일 경우 userId 추출 실패")
	void get_userId_failed_test() {
		// given
		final Long userId = 1L;
		final BaseRole role = BaseRole.USER;

		String accessToken = jwtUtil.generateAccessToken(userId, role);
		assertNotNull(accessToken);

		// when & then
		Assertions.assertThatThrownBy(() -> jwtUtil.getUserIdFromToken(accessToken + "ABCD"))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException) ex;
				assertEquals(ErrorCode.JWT_PROCESSING_FAILED, customException.getErrorCode());
				assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
				assertEquals("JWT 처리 중 오류가 발생했습니다.", customException.getErrorCode().getMessage());
			});
	}

	@Test
	@DisplayName("[액세스 토큰 재발급 성공 테스트] 리프래시 토큰이 유효하면 액세스 토큰 재발급 가능")
	void refresh_access_token_success_test () {
		// given
		SignUpRequestDto signUpRequest = SignUpRequestDto.builder()
			.loginId("test_login_id_token_test_1")
			.password("test_password")
			.name("test_name_token_test_1")
			.build();
		userService.createUser(signUpRequest);

		final Long userId = 1L;
		final BaseRole role = BaseRole.USER;

		String refreshToken = jwtUtil.generateRefreshToken(userId, role);
		assertNotNull(refreshToken);
		refreshTokenRepository.save(RefreshToken.of(userId, refreshToken));

		// when & then
		String accessToken = jwtUtil.refreshAccessToken(refreshToken);
		assertNotNull(accessToken);
	}

	@Test
	@DisplayName("[액세스 토큰 재발급 실패 테스트] 리프래시 토큰이 유효하지 않으면 액세스 토큰 재발급 불가능")
	void refresh_access_token_failed_test () {
		// given
		SignUpRequestDto signUpRequest = SignUpRequestDto.builder()
			.loginId("test_login_id_token_test_2")
			.password("test_password")
			.name("test_name_token_test_2")
			.build();
		userService.createUser(signUpRequest);

		final Long userId = 1L;
		final BaseRole role = BaseRole.USER;

		String refreshToken = jwtUtil.generateRefreshToken(userId, role);
		assert refreshToken != null;
		refreshTokenRepository.save(RefreshToken.of(userId, refreshToken));

		// when & then
		Assertions.assertThatThrownBy(() -> jwtUtil.refreshAccessToken(refreshToken + "abcd"))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException customException = (CustomException) ex;
				assertEquals(ErrorCode.REFRESH_TOKEN_EXPIRED, customException.getErrorCode());
				assertEquals(HttpStatus.UNAUTHORIZED, customException.getErrorCode().getHttpStatus());
				assertEquals("리프레시 토큰이 만료되었습니다.", customException.getErrorCode().getMessage());
			});
	}

	@Test
	@DisplayName("[토큰 유효성 성공 테스트] 정상적으로 발급된 토큰은 true 반환")
	void validate_token_success_test() {
		// given
		final Long userId = 1L;
		final BaseRole role = BaseRole.USER;

		String refreshToken = jwtUtil.generateRefreshToken(userId, role);

		// when & then
		boolean isValid = jwtUtil.validateToken(refreshToken);
		assertTrue(isValid);
	}

	@Test
	@DisplayName("[토큰 유효성 실패 테스트] 비정상적으로 발급된 토큰은 false 반환")
	void validate_token_failed_test() {
		// given
		final Long userId = 1L;
		final BaseRole role = BaseRole.USER;

		String refreshToken = jwtUtil.generateRefreshToken(userId, role);

		// when & then
		boolean isValid = jwtUtil.validateToken(refreshToken + "ABCD");
		assertFalse(isValid);
	}
}