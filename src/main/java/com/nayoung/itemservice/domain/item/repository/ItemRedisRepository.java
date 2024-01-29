package com.nayoung.itemservice.domain.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final String ITEM_PREFIX = "item:";
    private final String SUFFIX_QUANTITY = ":count";

    public void initializeItemStock(Long itemId, Long stock) {
        redisTemplate
                .opsForValue()
                .set(ITEM_PREFIX + itemId + SUFFIX_QUANTITY, stock.toString());
    }

    public void decrementItemStock(Long itemId, Long quantity) {
        redisTemplate
                .opsForValue()
                .decrement(ITEM_PREFIX + itemId + SUFFIX_QUANTITY, quantity);
    }

    public Long incrementItemStock(Long itemId, Long quantity) {
        return redisTemplate
                .opsForValue()
                .increment(ITEM_PREFIX + itemId + SUFFIX_QUANTITY, quantity);
    }
}
