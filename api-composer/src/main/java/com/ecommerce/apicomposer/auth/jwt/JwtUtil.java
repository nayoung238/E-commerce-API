package com.ecommerce.apicomposer.auth.jwt;

import com.ecommerce.apicomposer.common.exception.CustomException;
import com.ecommerce.apicomposer.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	public final JwtProperties jwtProperties;

	public final static String HEADER_AUTHORIZATION = "Authorization";
	public final static String TOKEN_PREFIX = "Bearer ";

	public Long getUserIdFromToken(String token) {
		try {
			Claims claims = getClaims(token);
			return claims.get("userId", Long.class);
		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.TOKEN_EXPIRED);
		} catch (JwtException e) {
			throw new CustomException(ErrorCode.JWT_PROCESSING_FAILED);
		}
	}

	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	Claims getClaims(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token)
			.getBody();
	}

	public String getAccessToken(HttpServletRequest httpServletRequest) {
		String header = httpServletRequest.getHeader(HEADER_AUTHORIZATION);
		if (header != null && header.startsWith(TOKEN_PREFIX)) {
			return header.split(" ", 2)[1];
		}
		return null;
	}
}
