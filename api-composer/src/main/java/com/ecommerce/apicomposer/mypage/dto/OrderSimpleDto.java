package com.ecommerce.apicomposer.mypage.dto;

public record OrderSimpleDto (

    Long orderId,
    String orderEventId,
    String orderName,
    String orderStatus,
    String requestedAt
) { }
