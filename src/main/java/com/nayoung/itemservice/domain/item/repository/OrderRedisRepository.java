package com.nayoung.itemservice.domain.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "order:";

    public Long addEventId(String key, String eventId) {
        return redisTemplate.opsForSet()
                .add(PREFIX + key, eventId);
    }
}