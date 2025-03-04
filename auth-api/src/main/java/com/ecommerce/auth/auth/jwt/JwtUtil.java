package com.ecommerce.auth.auth.jwt;

import com.ecommerce.auth.auth.enums.BaseRole;
import com.ecommerce.auth.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import com.ecommerce.auth.user.service.UserService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	public static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15;
	private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;
	private final JwtProperties jwtProperties;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserService userService;

	public String generateAccessToken(Long userId, BaseRole role) {
		return generateToken(userId, role, ACCESS_TOKEN_EXPIRATION);
	}

	public String generateRefreshToken(Long userId, BaseRole role) {
		return generateToken(userId, role, REFRESH_TOKEN_EXPIRATION);
	}

	private String generateToken(Long userId, BaseRole role, long expirationTime) {
		if (userId == null) {
			throw new CustomException(ErrorCode.MISSING_USER_ID);
		}
		if (role == null) {
			throw new CustomException(ErrorCode.MISSING_ROLE);
		}

		return Jwts.builder()
			.setIssuer(jwtProperties.getIssuer())
			.setSubject(userId.toString())
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + expirationTime))
			.claim("userId", userId)
			.claim("role", role.name())
			.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
			.compact();
	}

	public String refreshAccessToken(String refreshToken) {
		if (!validateToken(refreshToken)) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		boolean isExist = refreshTokenRepository.existsByRefreshToken(refreshToken);
		if (!isExist) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		try {
			Long userId = getUserIdFromToken(refreshToken);
			BaseRole role = userService.getRole(userId);
			return generateAccessToken(userId, role);
		} catch (CustomException e) {
			if (e.getErrorCode().equals(ErrorCode.TOKEN_EXPIRED)) {
				throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
			}
			throw e;
		}
	}

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

	public Claims getClaims(String token) {
		return Jwts.parser()
			.setSigningKey(jwtProperties.getSecretKey())
			.parseClaimsJws(token)
			.getBody();
	}
}
