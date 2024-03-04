package com.ecommerce.itemservice.domain.shop;

import com.ecommerce.itemservice.domain.shop.location.LocationCode;
import com.ecommerce.itemservice.domain.shop.dto.ShopDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LocationCode locationCode;

    @Column(unique = true)
    private String name;

    public static Shop fromShopDto(ShopDto shopDto) {
        return Shop.builder()
                .locationCode(LocationCode.getLocationCode(shopDto.getLocation()))
                .name(shopDto.getName())
                .build();
    }
}
