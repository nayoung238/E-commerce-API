package com.ecommerce.apicomposer.mypage.dto.response;

public record OrderSummaryResponse (

    Long orderId,
    String orderEventId,
    String orderName,
    String orderStatus,
    String requestedAt
) { }
