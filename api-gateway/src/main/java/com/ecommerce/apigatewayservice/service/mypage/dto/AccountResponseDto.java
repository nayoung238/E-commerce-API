package com.ecommerce.apigatewayservice.service.mypage.dto;

public record AccountResponseDto (

	Long accountId,
    String loginId,
    String name
) { }
