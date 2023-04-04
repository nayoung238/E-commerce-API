package com.nayoung.itemservice.domain.shop.location;

import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.LocationException;

public enum ProvinceCode {

    SEOUL, BUSAN, KYEONGGI, NONE;

    public static ProvinceCode getProvinceCode(String province) {
        if(province.equals("seoul")) return SEOUL;
        if(province.equals("busan")) return BUSAN;
        if(province.equals("kyeonggi")) return KYEONGGI;
        if(province.equals("none")) return NONE;
        throw new LocationException(ExceptionCode.NON_SERVICE_LOCATION);
    }
}
