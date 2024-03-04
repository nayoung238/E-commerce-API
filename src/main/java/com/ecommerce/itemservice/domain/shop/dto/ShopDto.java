package com.ecommerce.itemservice.domain.shop.dto;

import com.ecommerce.itemservice.domain.shop.Shop;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

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