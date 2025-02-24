package com.ecommerce.apicomposer.mypage.dto;

import lombok.Getter;

@Getter
public class OrderSimpleDto {

    private Long orderId;
    private String orderEventId;
    private String orderName;
    private String orderStatus;
    private String requestedAt;
}
