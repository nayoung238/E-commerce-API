package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.openfeign.ItemUpdateStatus;
import lombok.Getter;

@Getter
public class ItemUpdateLogDto {

    private Long id;
    private ItemUpdateStatus itemUpdateStatus;
    private Long orderId;
    private Long customerAccountId;
    private String createdAt;
    private Long itemId;
    private Long quantity;
}