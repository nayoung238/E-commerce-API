package com.nayoung.itemservice.domain.shop;

import com.nayoung.itemservice.domain.shop.location.Location;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Location location;

    private Shop(Location location) {
        this.location = location;
    }

    public static Shop fromLocation(Location location) {
        return new Shop(location);
    }
}
