package com.nayoung.itemservice.domain.shop.location;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.LocationException;

public enum CityCode {

    SEOUL, BUSAN, SUWON, GUNPO, NONE;

    public static CityCode getCityCode(String cityCode) {
        if(cityCode.equals("seoul")) return SEOUL;
        if(cityCode.equals("busan")) return BUSAN;
        if(cityCode.equals("suwon")) return SUWON;
        if(cityCode.equals("gunpo")) return GUNPO;
        if(cityCode.equals("none")) return NONE;
        throw new LocationException(ExceptionCode.NON_SERVICE_LOCATION);
    }
}
