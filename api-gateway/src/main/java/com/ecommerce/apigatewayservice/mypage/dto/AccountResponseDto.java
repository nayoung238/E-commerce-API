package com.ecommerce.apigatewayservice.mypage.dto;

public record AccountResponseDto (

	Long accountId,
    String loginId,
    String name
) { }
