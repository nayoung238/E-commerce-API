package com.nayoung.itemservice.domain.shop.location;

import lombok.Getter;

import javax.persistence.*;

@Embeddable
@Getter
public class Location {

    @Enumerated(EnumType.STRING)
    @AttributeOverride(name = "province", column = @Column(name = "province"))
    private ProvinceCode province;

    @Enumerated(EnumType.STRING)
    @AttributeOverride(name = "city", column = @Column(name = "city"))
    private CityCode city;

    protected Location() {}

    public Location(String province, String city) {
        this.province = ProvinceCode.getProvinceCode(province);
        this.city = CityCode.getCityCode(city);
    }
}
