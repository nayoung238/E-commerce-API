package com.ecommerce.accountservice.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public record SignUpRequestDto (

    String loginId,
    String password,
    String name
) { }
