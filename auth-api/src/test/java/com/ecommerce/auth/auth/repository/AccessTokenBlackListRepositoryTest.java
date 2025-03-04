package com.ecommerce.auth.auth.repository;

import com.ecommerce.auth.auth.jwt.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties(JwtProperties.class)
class AccessTokenBlackListRepositoryTest {

	@Autowired
	private AccessTokenBlackListRepository accessTokenBlackListRepository;

	@Test
	@DisplayName("[Blacklist 성공 테스트] blacklist 추가 시 true 반환")
	void blacklist_success_test() {
		// given
		final String test_token = "test_token";
		final long expirationMillis = 1000 * 60 * 5;

		accessTokenBlackListRepository.addToBlackList(test_token, expirationMillis);

		boolean isBlacklisted = accessTokenBlackListRepository.isBlackListed(test_token);
		assertTrue(isBlacklisted);
	}

	@Test
	@DisplayName("[Blacklist 실패 테스트] blacklist에 존재하지 않으면 false 반환")
	void blacklist_failed_test() {
		// when
		boolean isBlacklisted = accessTokenBlackListRepository.isBlackListed("test_token_abcd_1234");
		assertFalse(isBlacklisted);
	}
}