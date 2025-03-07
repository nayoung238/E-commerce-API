package com.ecommerce.auth.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SignUpRequest(

	@NotBlank(message = "로그인 아이디는 필수입니다.")
    String loginId,

	@NotBlank(message = "비밀번호는 필수입니다.")
    String password,

	@NotBlank(message = "이름은 필수입니다.")
    String name
) { }
