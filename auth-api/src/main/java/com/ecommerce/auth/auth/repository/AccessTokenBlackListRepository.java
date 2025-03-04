package com.ecommerce.auth.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class AccessTokenBlackListRepository {

	private static final String BLACKLIST_PREFIX = "access_token:blacklist:";
	private final RedisTemplate<String, String> redisTemplate;

	public void addToBlackList(String accessToken, long expirationMillis) {
		redisTemplate.opsForValue().set(
			BLACKLIST_PREFIX + accessToken,
			"blacklisted",
			Duration.ofMillis(expirationMillis)
		);
	}

	public boolean isBlackListed(String accessToken) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
	}
}
