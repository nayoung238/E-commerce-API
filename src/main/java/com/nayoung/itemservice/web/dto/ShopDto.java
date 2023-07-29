package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.shop.Shop;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class ShopDto {

    private Long id;

    @NotBlank
    private String location;

    @NotBlank
    private String name;

    public static ShopDto fromShop(Shop shop) {
        return ShopDto.builder()
                .id(shop.getId())
                .location(shop.getLocationCode().toString())
                .name(shop.getName())
                .build();
    }
}