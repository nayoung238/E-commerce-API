package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopCreationRequest {

    private String province;
    private String city;

    @Builder
    public ShopCreationRequest(String province, String city) {
        this.province = province;
        this.city = city;
    }
}
