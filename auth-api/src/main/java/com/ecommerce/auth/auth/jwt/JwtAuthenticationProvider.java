package com.ecommerce.auth.auth.jwt;

import com.ecommerce.auth.auth.entity.UserPrincipal;
import com.ecommerce.auth.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {

	private final JwtUtil jwtUtil;
	private final AuthService authService;

	public Authentication getAuthentication(String token) {
		Claims claims = jwtUtil.getClaims(token);

		Long userId = claims.get("userId", Long.class);
		UserPrincipal userPrincipal = authService.getUserPrincipal(userId);

		String role = claims.get("role", String.class);
		Set<SimpleGrantedAuthority> authorities = getRoles(role);

		return new UsernamePasswordAuthenticationToken(userPrincipal, token, authorities);
	}

	public Set<SimpleGrantedAuthority> getRoles(String role) {
		if (role.equals("ADMIN")) {
			return Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
	}
}
