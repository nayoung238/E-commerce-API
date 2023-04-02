package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemInfoRequest {

    private Long itemId;
    private String customerRating;

    @Builder
    public ItemInfoRequest(Long itemId,  String customerRating) {
        this.itemId = itemId;
        this.customerRating = customerRating;
    }
}