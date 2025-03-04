package com.ecommerce.auth.auth.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@NoArgsConstructor
@AllArgsConstructor
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

	private String issuer;
	private String secretKey;
}
