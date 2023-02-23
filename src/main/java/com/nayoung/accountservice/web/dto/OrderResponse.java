package com.nayoung.accountservice.web.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponse {

    private Long itemId;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;

    private Long accountId;

    private LocalDateTime createdAt;
}
