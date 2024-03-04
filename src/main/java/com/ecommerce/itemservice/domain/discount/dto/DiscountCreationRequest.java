package com.ecommerce.itemservice.domain.discount.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DiscountCreationRequest {

    private Long itemId;
    private Integer discountPercentage;

    @Builder
    public DiscountCreationRequest(Long itemId, Integer discountPercentage) {
        this.itemId = itemId;
        this.discountPercentage = discountPercentage;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
