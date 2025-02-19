package com.ecommerce.accountservice.account.dto;

import lombok.Builder;

@Builder
public record SignUpRequestDto (

    String loginId,
    String password,
    String name
) { }
