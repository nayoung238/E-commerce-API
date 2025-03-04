package com.ecommerce.auth.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@Builder
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7)
@AllArgsConstructor
public class RefreshToken implements Serializable {

	@Id
	private Long userId;

	@Indexed
	private String refreshToken;

	public static RefreshToken of(Long userId, String refreshToken) {
		return RefreshToken.builder()
			.userId(userId)
			.refreshToken(refreshToken)
			.build();
	}
}
