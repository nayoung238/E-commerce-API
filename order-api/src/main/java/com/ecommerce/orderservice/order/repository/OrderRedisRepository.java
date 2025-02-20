package com.ecommerce.orderservice.order.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "order-event:";

    public Long addOrderEventKey(String key, String orderEventKey) {
        return redisTemplate.opsForSet()
                .add(PREFIX + key, orderEventKey);
    }
}
