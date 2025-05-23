package com.ecommerce.couponservice.common.config;

import com.ecommerce.couponservice.auth.jwt.JwtAuthenticationProvider;
import com.ecommerce.couponservice.auth.jwt.JwtUtil;
import com.ecommerce.couponservice.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final JwtAuthenticationProvider jwtAuthenticationProvider;

	public final static String HEADER_AUTHORIZATION = "Authorization";

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest,
									HttpServletResponse httpServletResponse,
									FilterChain filterChain) throws ServletException, IOException {

		String authorizationHeader = httpServletRequest.getHeader(HEADER_AUTHORIZATION);
		String token = jwtUtil.extractAccessToken(authorizationHeader);

		if (jwtUtil.validateToken(token)) {
			Authentication authentication = jwtAuthenticationProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} else if (token != null) {
			handleException(httpServletResponse, ErrorCode.INVALID_TOKEN);
			return;
		}

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

	private void handleException(HttpServletResponse httpServletResponse, ErrorCode errorCode) throws IOException {
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");
		httpServletResponse.setStatus(errorCode.getHttpStatus().value());
		httpServletResponse.getWriter().write("{\"message\": \"" + errorCode.getMessage() + "\"}");
	}
}
