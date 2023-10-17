package com.nayoung.itemservice.domain.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "order:";

    public Long addOrderId(String key, Long orderId) {
        return redisTemplate.opsForSet()
                .add(PREFIX + key, String.valueOf(orderId));  // key -> order:yyyy-mm-dd'T'HH
    }

    public Long addEventId(String key, String eventId) {
        return redisTemplate.opsForSet()
                .add(PREFIX + key, eventId);
    }
}