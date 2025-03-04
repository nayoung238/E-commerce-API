package com.ecommerce.auth.auth.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationProvider {

	private final JwtUtil jwtUtil;

	public Authentication getAuthentication(String token) {
		Claims claims = jwtUtil.getClaims(token);
		String role = claims.get("role", String.class);
		Set<SimpleGrantedAuthority> authorities = getRoles(role);

		return new UsernamePasswordAuthenticationToken(
			new User(claims.getSubject(), "", authorities), token, authorities);
	}

	public Set<SimpleGrantedAuthority> getRoles(String role) {
		if (role.equals("ADMIN")) {
			return Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
	}
}
