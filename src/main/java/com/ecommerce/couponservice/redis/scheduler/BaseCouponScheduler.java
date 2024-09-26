package com.ecommerce.couponservice.redis.scheduler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseCouponScheduler {

    protected Long extractCouponId(String keyPrefix, String key) {
        String regex = "^" + Pattern.quote(keyPrefix) + "(\\d+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(key);
        if(matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}
