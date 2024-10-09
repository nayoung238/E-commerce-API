package com.ecommerce.couponservice.redis.manager;

public class BaseRedisManager {

    public static final String COUPON_KEY = "coupon:";
    public static final String WAIT_KEY_PREFIX = "coupon:wait-queue:";
    public static final String ENTER_KEY_PREFIX = "coupon:enter-queue:";

    public static String getCouponKey(Long couponId) {
        return COUPON_KEY + couponId;
    }

    public static String getWaitQueueKey(Long couponId) {
        return WAIT_KEY_PREFIX + couponId;
    }

    public static String getEnterQueueKey(Long couponId) {
        return ENTER_KEY_PREFIX + couponId;
    }
}