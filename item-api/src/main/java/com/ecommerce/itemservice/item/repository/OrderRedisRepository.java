package com.ecommerce.itemservice.item.repository;

import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String PREFIX = "order:";
    private final String ORDER_EVENT_KEY_PREFIX = "order:event-key:";

    public Long addEventId(String key, String eventId) {
        return redisTemplate.opsForSet()
                .add(PREFIX + key, eventId);
    }

    public void setOrderProcessingStatus(String eventId, OrderProcessingStatus orderItemStatus) {
        redisTemplate
                .opsForValue()
                .set(ORDER_EVENT_KEY_PREFIX + eventId, String.valueOf(orderItemStatus));
    }

    public String getOrderProcessingStatus(String orderEventKey) {
        return redisTemplate
                .opsForValue()
                .get(ORDER_EVENT_KEY_PREFIX + orderEventKey);
    }
}