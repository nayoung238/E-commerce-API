package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.domain.shop.location.LocationCode;
import com.nayoung.itemservice.web.dto.ShopDto;
import lombok.*;

import javax.persistence.*;

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
