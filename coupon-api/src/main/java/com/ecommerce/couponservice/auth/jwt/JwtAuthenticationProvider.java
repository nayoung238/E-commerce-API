package com.ecommerce.couponservice.auth.jwt;

import com.ecommerce.couponservice.auth.entity.UserPrincipal;
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

	public Authentication getAuthentication(String token) {
		Claims claims = jwtUtil.getClaims(token);

		Long userId = claims.get("userId", Long.class);
		UserPrincipal userPrincipal = UserPrincipal.builder()
			.id(userId)
			.build();

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
