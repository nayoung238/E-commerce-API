package com.ecommerce.apigatewayservice.service.mypage.dto;

import lombok.Getter;

@Getter
public class SimpleAccountDto {

    private long accountId;
    private String email;
    private String name;
    private long numberOfCoupons;
}
