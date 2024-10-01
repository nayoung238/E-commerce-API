package com.ecommerce.couponservice.redis.manager;

public class BaseRedisManager {

    public static final String COUPON_STOCK_KEY = "coupon:stock";
    public static final String WAIT_KEY_PREFIX = "coupon:wait-queue:";
    public static final String ENTER_KEY_PREFIX = "coupon:enter-queue:";

    public static String getWaitQueueKey(Long couponId) {
        return WAIT_KEY_PREFIX + couponId;
    }

    public static String getEnterQueueKey(Long couponId) {
        return ENTER_KEY_PREFIX + couponId;
    }
}