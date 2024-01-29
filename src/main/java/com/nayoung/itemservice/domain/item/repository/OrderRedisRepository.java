package com.nayoung.itemservice.domain.item.repository;

import com.nayoung.itemservice.kafka.dto.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "order:";
    private final String ORDER_EVENT_ID_PREFIX = "order:eventid:";

    public Long addEventId(String key, String eventId) {
        return redisTemplate.opsForSet()
                .add(PREFIX + key, eventId);
    }

    public void setOrderStatus(String eventId, OrderItemStatus orderItemStatus) {
        redisTemplate
                .opsForValue()
                .set(ORDER_EVENT_ID_PREFIX + eventId, String.valueOf(orderItemStatus));
    }

    public String getOrderStatus(String eventId) {
        return redisTemplate
                .opsForValue()
                .get(ORDER_EVENT_ID_PREFIX + eventId);
    }
}