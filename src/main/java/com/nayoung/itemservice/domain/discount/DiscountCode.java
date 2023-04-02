package com.nayoung.itemservice.domain.discount;

import com.nayoung.itemservice.exception.DiscountException;
import com.nayoung.itemservice.exception.ExceptionCode;

import java.util.Objects;

public enum DiscountCode {

    NONE(0), SILVER(5), GOLD(10);

    public final int percentage;

    DiscountCode(int percentage) {
        this.percentage = percentage;
    }

    public static DiscountCode getDiscountCode(String customerRating) {
        if(Objects.equals(customerRating, "UNQUALIFIED")) return DiscountCode.NONE;
        if(Objects.equals(customerRating, "WELCOME")) return DiscountCode.NONE;
        if(Objects.equals(customerRating, "SILVER")) return DiscountCode.SILVER;
        if(Objects.equals(customerRating, "GOLD")) return DiscountCode.GOLD;
        throw new DiscountException(ExceptionCode.NO_MATCHING_DISCOUNT_CODE);
    }
}
