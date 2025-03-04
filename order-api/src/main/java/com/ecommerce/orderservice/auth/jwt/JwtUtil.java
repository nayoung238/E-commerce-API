package com.ecommerce.orderservice.auth.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	private final JwtProperties jwtProperties;

	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Claims getClaims(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token)
			.getBody();
	}
}
