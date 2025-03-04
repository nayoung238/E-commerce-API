package com.ecommerce.auth.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoginRequest (

	@NotNull(message = "로그인 아이디는 필수입니다.")
	String loginId,

	@NotNull(message = "비밀번호는 필수입니다.")
	String password
) { }
