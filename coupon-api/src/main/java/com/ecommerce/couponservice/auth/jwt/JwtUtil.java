package com.ecommerce.couponservice.auth.jwt;

import com.ecommerce.couponservice.common.exception.CustomException;
import com.ecommerce.couponservice.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	private final JwtProperties jwtProperties;

	public static final String TOKEN_PREFIX = "Bearer ";

	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Long getUserIdFromRequestHeader(String requestHeader) {
		String accessToken = extractAccessToken(requestHeader);
		if (accessToken == null) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_FORMAT_INVALID);
		}

		try {
			Claims claims = getClaims(accessToken);
			return claims.get("userId", Long.class);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_FORMAT_INVALID);
		}
	}

	public String extractAccessToken(String requestHeader) {
		if (requestHeader != null && requestHeader.startsWith(TOKEN_PREFIX)) {
			return requestHeader.split(" ", 2)[1];
		}
		return null;
	}

	public Claims getClaims(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token)
			.getBody();
	}
}
