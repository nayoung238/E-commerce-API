package com.ecommerce.accountservice.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public record SignUpRequestDto (

    String loginId,
    String password,
    String name
) { }
