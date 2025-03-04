package com.ecommerce.auth.auth.service;

import com.ecommerce.auth.auth.dto.request.LoginRequest;
import com.ecommerce.auth.auth.entity.RefreshToken;
import com.ecommerce.auth.auth.entity.UserPrincipal;
import com.ecommerce.auth.auth.jwt.JwtUtil;
import com.ecommerce.auth.auth.repository.AccessTokenBlackListRepository;
import com.ecommerce.auth.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.common.config.JwtAuthenticationFilter;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import com.ecommerce.auth.user.entity.User;
import com.ecommerce.auth.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	private final AccessTokenBlackListRepository accessTokenBlackListRepository;

	private final static String COOKIE_REFRESH_TOKEN_NAME = "refresh_token";

	public void login(LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
		User user = userService.findUserEntity(loginRequest.loginId());
		user.verifyPasswordMatching(loginRequest.password());

		String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
		String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getRole());

		refreshTokenRepository.save(RefreshToken.of(user.getId(), refreshToken));

		httpServletResponse.setHeader(JwtAuthenticationFilter.HEADER_AUTHORIZATION,
									JwtAuthenticationFilter.TOKEN_PREFIX + accessToken);

		Cookie cookie = new Cookie(COOKIE_REFRESH_TOKEN_NAME, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24 * 7);
		httpServletResponse.addCookie(cookie);
	}

	public void refreshAccessToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String refreshToken = getRefreshToken(httpServletRequest);
		if (refreshToken == null) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}

		String accessToken = jwtUtil.refreshAccessToken(refreshToken);
		httpServletResponse.setHeader(JwtAuthenticationFilter.HEADER_AUTHORIZATION,
									JwtAuthenticationFilter.TOKEN_PREFIX + accessToken);
	}

	public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String refreshToken = getRefreshToken(httpServletRequest);
		if (refreshToken != null) {
			Long userId = jwtUtil.getUserIdFromToken(refreshToken);
			refreshTokenRepository.deleteById(userId);
		}

		String accessToken = getAccessToken(httpServletRequest);
		if (accessToken != null) {
			accessTokenBlackListRepository.addToBlackList(accessToken, JwtUtil.ACCESS_TOKEN_EXPIRATION);
		}

		Cookie cookie = new Cookie(COOKIE_REFRESH_TOKEN_NAME, null);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		httpServletResponse.addCookie(cookie);
	}

	private String getRefreshToken(HttpServletRequest httpServletRequest) {
		Cookie[] cookies = httpServletRequest.getCookies();
		if (cookies == null) {
			return null;
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(COOKIE_REFRESH_TOKEN_NAME)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private String getAccessToken(HttpServletRequest httpServletRequest) {
		String header = httpServletRequest.getHeader(JwtAuthenticationFilter.HEADER_AUTHORIZATION);
		if (header != null && header.startsWith(JwtAuthenticationFilter.TOKEN_PREFIX)) {
			return header.split(" ", 2)[1];
		}
		return null;
	}

	public UserPrincipal getUserPrincipal(Long userId) {
		User user = userService.findUserEntity(userId);
		return UserPrincipal.of(user);
	}
}
