package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopCreationRequest {

    private String province;
    private String city;
    private String name;

    @Builder
    public ShopCreationRequest(String province, String city, String name) {
        this.province = province;
        this.city = city;
        this.name = name;
    }
}
