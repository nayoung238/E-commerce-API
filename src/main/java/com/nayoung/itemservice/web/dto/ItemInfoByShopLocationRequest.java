package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemInfoByShopLocationRequest {

    private String itemName;
    private String customerRating;
    private String province;
    private String city;

    @Builder
    public ItemInfoByShopLocationRequest(String itemName, String customerRating, String province, String city) {
        this.itemName = itemName;
        this.customerRating = customerRating;
        this.province = province;
        this.city = city;
    }
}