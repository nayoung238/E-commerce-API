package com.ecommerce.accountservice.account.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SignUpRequestDto (

	@NotEmpty(message = "로그인 아이디는 필수입니다.")
    String loginId,

	@NotEmpty(message = "비밀번호는 필수입니다.")
    String password,

	@NotNull(message = "이름은 필수입니다.")
    String name
) { }
