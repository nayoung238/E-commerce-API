package com.ecommerce.apigatewayservice.service.mypage.dto;

import lombok.Getter;

@Getter
public class OrderSimpleDto {

    private String orderEventId;
    private String orderName;
    private String orderStatus;
    private String requestedAt;
}
