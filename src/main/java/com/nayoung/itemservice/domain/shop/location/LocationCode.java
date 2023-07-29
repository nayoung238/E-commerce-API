package com.nayoung.itemservice.domain.shop.location;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.LocationException;

public enum LocationCode {

    SEOUL, BUSAN, SUWON, GUNPO, NONE;

    public static LocationCode getLocationCode(String location) {
        if(location.equals("seoul")) return SEOUL;
        if(location.equals("busan")) return BUSAN;
        if(location.equals("suwon")) return SUWON;
        if(location.equals("gunpo")) return GUNPO;
        if(location.equals("none")) return NONE;
        throw new LocationException(ExceptionCode.NON_SERVICE_LOCATION);
    }
}
