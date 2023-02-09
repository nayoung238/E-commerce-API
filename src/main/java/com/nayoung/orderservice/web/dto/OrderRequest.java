package com.nayoung.orderservice.web.dto;

import lombok.Data;

@Data
public class OrderRequest {

    private Long itemId;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;

    private Long accountId;
}
