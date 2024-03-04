package com.ecommerce.itemservice.domain.item.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemInfoByItemIdRequest {

    private Long itemId;
    private String customerRating;

    @Builder
    public ItemInfoByItemIdRequest(Long itemId, String customerRating) {
        this.itemId = itemId;
        this.customerRating = customerRating;
    }
}
