package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemInfoByShopLocationRequest {

    private String itemName;
    private String customerRating;
    private String city;

    @Builder
    public ItemInfoByShopLocationRequest(String itemName, String customerRating, String city) {
        this.itemName = itemName;
        this.customerRating = customerRating;
        this.city = city;
    }
}