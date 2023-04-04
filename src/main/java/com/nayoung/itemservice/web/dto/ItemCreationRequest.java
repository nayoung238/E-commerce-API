package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemCreationRequest {

    private Long shopId;
    private String name;
    private Long price;
    private Long stock;

    @Builder
    public ItemCreationRequest(Long shopId, String name, Long price, Long stock) {
        this.shopId = shopId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
}
